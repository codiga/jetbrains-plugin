package io.codiga.plugins.jetbrains.annotators;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiManager;
import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCacheValue.RuleWithNames;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil;
import io.codiga.plugins.jetbrains.rosie.model.codiga.CodigaYmlConfig;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches Rosie rules based on the most up-to-date version of rules and rulesets on the Codiga server.
 * <p>
 * It is used as a {@link Disposable} project service, so that upon closing the project, the caches can be
 * cleaned up as part of the disposal process.
 */
@Service(Service.Level.PROJECT)
public final class RosieRulesCache implements Disposable {

    private final Project project;
    /**
     * Mapping the rules to their target Rosie languages, because this way
     * <ul>
     *     <li>retrieving the rules from this cache is much easier,</li>
     *     <li>filtering the rules by language each time a request has to be sent to
     *     the Rosie service is not necessary.</li>
     * </ul>
     * <p>
     * NOTE: in the future, when the codiga.yml config file will be recognized at locations other than the project root,
     * the cache key will probably have to be changed.
     */
    private final Map<LanguageEnumeration, RosieRulesCacheValue> cache;
    /**
     * The timestamp of the last update on the Codiga server for the rulesets cached (and configured in codiga.yml).
     */
    @Getter
    @Setter
    private long lastUpdatedTimeStamp = -1L;
    /**
     * -1 means the modification stamp of codiga.yml hasn't been set,
     * or there is no codiga.yml file in the project root.
     */
    private long configFileModificationStamp = -1L;
    /**
     * The codiga.yml configuration in the current project.
     */
    @Getter
    private CodigaYmlConfig codigaYmlConfig;
    /**
     * [ruleset name] -> [is ruleset empty]
     * <p>
     * The ruleset names are the ones returned from the Codiga server after sending the local {@code codigaYmlConfig#getRulesets()}.
     * <p>
     * If a locally configured ruleset name is not returned (it doesn't exist on Codiga Hub), it won't have an entry in this collection.
     */
    private final Map<String, Boolean> rulesetsFromServer;
    /**
     * Stores if {@link #updateCacheFrom(List)} has been called at least once.
     */
    @Getter
    private boolean isInitialized = false;

    public RosieRulesCache(Project project) {
        this.project = project;
        this.cache = new ConcurrentHashMap<>();
        this.codigaYmlConfig = CodigaYmlConfig.EMPTY;
        this.rulesetsFromServer = new ConcurrentHashMap<>();
    }

    public boolean hasDifferentModificationStampThan(YAMLFile codigaConfigFile) {
        return codigaConfigFile.getModificationStamp() != configFileModificationStamp;
    }

    public void saveModificationStampOf(YAMLFile codigaConfigFile) {
        this.configFileModificationStamp = codigaConfigFile.getModificationStamp();
    }

    public void setCodigaYmlConfig(@NotNull CodigaYmlConfig codigaYmlConfig) {
        this.codigaYmlConfig = codigaYmlConfig;
    }

    public boolean isRulesetExist(String rulesetName) {
        return rulesetsFromServer.containsKey(rulesetName);
    }

    public boolean isRulesetEmpty(String rulesetName) {
        return rulesetsFromServer.get(rulesetName);
    }

    /**
     * Clears and repopulates this cache based on the argument 'rulesets' information returned
     * from the Codiga API.
     * <p>
     * Groups the rules by their target languages, converts them to {@code RosieRule} objects,
     * and wraps and stores them in {@link RosieRulesCacheValue}s.
     *
     * @param rulesetsFromCodigaAPI the rulesets information
     */
    public void updateCacheFrom(List<GetRulesetsForClientQuery.RuleSetsForClient> rulesetsFromCodigaAPI) {
        saveRulesets(rulesetsFromCodigaAPI);
        saveRulesByLanguages(rulesetsFromCodigaAPI);
        reAnalyzeConfigFile();
        isInitialized = true;
    }

    private void saveRulesets(List<GetRulesetsForClientQuery.RuleSetsForClient> rulesetsFromCodigaAPI) {
        var rulesets = rulesetsFromCodigaAPI.stream()
            .collect(toMap(GetRulesetsForClientQuery.RuleSetsForClient::name, entry -> entry.rules().isEmpty()));
        rulesetsFromServer.clear();
        rulesetsFromServer.putAll(rulesets);
    }

    private void saveRulesByLanguages(List<GetRulesetsForClientQuery.RuleSetsForClient> rulesetsFromCodigaAPI) {
        var rulesByLanguage = rulesetsFromCodigaAPI.stream()
            .flatMap(ruleset -> ruleset.rules().stream().map(rule -> new RuleWithNames(ruleset.name(), rule)))
            .collect(groupingBy(rule -> rule.rosieRule.language))
            .entrySet()
            .stream()
            .collect(toMap(entry -> LanguageEnumeration.safeValueOf(entry.getKey()), entry -> new RosieRulesCacheValue(entry.getValue())));
        //Clearing and repopulating the cache is easier than picking out one by one
        // the ones that remain, and the ones that have to be removed.
        cache.clear();
        cache.putAll(rulesByLanguage);
    }

    /**
     * Restarts the analysis and highlight process of the Codiga config file, so that the highlighting in the config file
     * always reflects the current state of the cache.
     */
    private void reAnalyzeConfigFile() {
        Arrays.stream(FileEditorManager.getInstance(project).getOpenFiles())
            .filter(file -> CodigaConfigFileUtil.CODIGA_CONFIG_FILE_NAME.equals(file.getName()))
            .map(file -> ReadAction.compute(() -> PsiManager.getInstance(project).findFile(file)))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(file -> DaemonCodeAnalyzer.getInstance(project).restart(file));
    }

    /**
     * Returns the list of {@code RosieRule}s for the argument language and file path,
     * that will be sent to the Rosie service for analysis.
     *
     * @param language           the language to return the rules for, or empty list if there is no rule cached for the
     *                           provided language
     * @param pathOfAnalyzedFile the absolute path of the file being analyzed. Required to pass in for the {@code ignore} configuration.
     */
    public List<RosieRule> getRosieRules(LanguageEnumeration language, @NotNull String pathOfAnalyzedFile) {
        var projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir != null) {
            var cachedRules = cache.get(getCachedLanguageTypeOf(language));
            var rosieRulesForLanguage = cachedRules != null ? cachedRules.getRosieRules() : List.<RosieRule>of();

            if (!rosieRulesForLanguage.isEmpty()) {
                String relativePathOfAnalyzedFile = pathOfAnalyzedFile.replace(projectDir.getPath(), "");
                //Returns the RosieRules that either don't have an ignore rule, or their prefixes don't match the currently analyzed file's path
                return rosieRulesForLanguage.stream()
                    .filter(rosieRule -> {
                            var ruleIgnore = Optional.of(codigaYmlConfig)
                                .map(config -> config.getIgnore(rosieRule.rulesetName))
                                .map(rulesetIgnore -> rulesetIgnore.getRuleIgnore(rosieRule.ruleName));

                            //If there is no ruleset ignore or rule ignore for the current RosieRule,
                            // then we keep it/don't ignore it.
                            if (ruleIgnore.isEmpty())
                                return true;

                            //If there is no prefix specified for the current rule ignore config,
                            // we don't keep the rule/ignore it.
                            if (ruleIgnore.get().getPrefixes().isEmpty())
                                return false;

                            return ruleIgnore.get().getPrefixes().stream()
                                //Since the leading / is optional, we remove it
                                .map(this::removeLeadingSlash)
                                //./, /. and .. sequences are not allowed in prefixes, therefore we consider them not matching the file path.
                                //. symbols in general are allowed to be able to target exact file paths with their file extensions.
                                .noneMatch(prefix ->
                                    !prefix.contains("..")
                                        && !prefix.contains("./")
                                        && !prefix.contains("/.")
                                        && removeLeadingSlash(relativePathOfAnalyzedFile).startsWith(prefix));
                        }
                    ).collect(toList());
            }
        }

        return List.of();
    }

    private String removeLeadingSlash(String path) {
        return path.startsWith("/") ? path.replaceFirst("/", "") : path;
    }

    /**
     * Since, besides JavaScript files, rules for TypeScript files are also handled under the same JavaScript Rosie language
     * type, we have to return JavaScript rules for TypeScript files as well.
     *
     * @param fileLanguage the file language to map
     */
    private LanguageEnumeration getCachedLanguageTypeOf(LanguageEnumeration fileLanguage) {
        return fileLanguage == LanguageEnumeration.TYPESCRIPT ? LanguageEnumeration.JAVASCRIPT : fileLanguage;
    }

    /**
     * Returns the cached rules for the provided language and rule id.
     * <p>
     * Null value for non-existent mapping for a language is already handled in {@link #getRosieRules(LanguageEnumeration, String)}.
     * <p>
     * It should not return null when retrieving the rule for the rule id, since in {@code RosieImpl#getAnnotations()}
     * the {@link io.codiga.plugins.jetbrains.model.rosie.RosieRuleResponse}s and their ids are based on the values
     * cached here.
     */
    public RuleWithNames getRuleWithNamesFor(LanguageEnumeration language, String ruleId) {
        return cache.get(getCachedLanguageTypeOf(language)).getRules().get(ruleId);
    }

    /**
     * Empties the cache if it is not empty.
     */
    public void clear() {
        if (!cache.isEmpty()) {
            cache.clear();
        }
        if (!rulesetsFromServer.isEmpty()) {
            rulesetsFromServer.clear();
        }
        codigaYmlConfig = CodigaYmlConfig.EMPTY;
        lastUpdatedTimeStamp = -1L;
    }

    @Override
    public void dispose() {
        clear();
    }

    public static RosieRulesCache getInstance(@NotNull Project project) {
        return project.getService(RosieRulesCache.class);
    }

    //For testing

    @TestOnly
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @TestOnly
    public long getConfigFileModificationStamp() {
        return configFileModificationStamp;
    }

    @TestOnly
    public void setConfigFileModificationStamp(long configFileModificationStamp) {
        this.configFileModificationStamp = configFileModificationStamp;
    }
}
