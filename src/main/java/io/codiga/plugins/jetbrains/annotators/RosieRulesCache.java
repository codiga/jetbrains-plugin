package io.codiga.plugins.jetbrains.annotators;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCacheValue.RuleWithNames;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Caches Rosie rules based on the most up-to-date version of rules and rulesets on the Codiga server.
 * <p>
 * It is used as a {@link Disposable} project service, so that upon closing the project, the caches can be
 * cleaned up as part of the disposal process.
 */
@Service(Service.Level.PROJECT)
public final class RosieRulesCache implements Disposable {

    /**
     * Mapping the rules to their target languages, because this way
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
     * Ruleset names from the codiga.yml config file.
     */
    @Getter
    private List<String> rulesetNames;

    public RosieRulesCache(Project project) {
        this.cache = new ConcurrentHashMap<>();
        this.rulesetNames = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean hasDifferentModificationStampThan(YAMLFile codigaConfigFile) {
        return codigaConfigFile.getModificationStamp() != configFileModificationStamp;
    }

    public void saveModificationStampOf(YAMLFile codigaConfigFile) {
        this.configFileModificationStamp = codigaConfigFile.getModificationStamp();
    }

    public void setRulesetNames(List<String> rulesetNames) {
        this.rulesetNames = Collections.synchronizedList(rulesetNames);
    }

    /**
     * Clears and repopulates this cache based on the argument rulesets' information returned
     * from the Codiga API.
     * <p>
     * Groups the rules by their target languages, converts them to {@code RosieRule} objects,
     * and wraps and stores them in {@link RosieRulesCacheValue}s.
     *
     * @param rulesetsFromCodigaAPI the rulesets information
     */
    public void updateCacheFrom(List<GetRulesetsForClientQuery.RuleSetsForClient> rulesetsFromCodigaAPI) {
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
     * Returns the list of {@code RosieRule}s for the argument language,
     * that will be sent to the Rosie service for analysis.
     *
     * @param language the language to return the rules for, or empty list if there is no rule cached for the
     *                 provided language
     */
    public List<RosieRule> getRosieRulesForLanguage(LanguageEnumeration language) {
        var cachedRules = cache.get(language);
        return cachedRules != null ? cachedRules.getRosieRules() : List.of();
    }

    /**
     * Returns the cached rules for the provided language and rule id.
     * <p>
     * Null value for non-existent mapping for a language is already handled in {@link #getRosieRulesForLanguage(LanguageEnumeration)}.
     * <p>
     * It should not return null when retrieving the rule for the rule id, since in {@code RosieImpl#getAnnotations()}
     * the {@link io.codiga.plugins.jetbrains.model.rosie.RosieRuleResponse}s and their ids are based on the values
     * cached here.
     */
    public RuleWithNames getRuleWithNamesFor(LanguageEnumeration language, String ruleId) {
        return cache.get(language).getRules().get(ruleId);
    }

    /**
     * Empties the cache if it is not empty.
     */
    public void clear() {
        if (!cache.isEmpty()) {
            cache.clear();
        }
        if (!rulesetNames.isEmpty()) {
            rulesetNames.clear();
        }
        lastUpdatedTimeStamp = -1L;
    }

    @Override
    public void dispose() {
        clear();
    }

    public static RosieRulesCache getInstance(@NotNull Project project) {
        return project.getService(RosieRulesCache.class);
    }

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
