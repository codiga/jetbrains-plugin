package io.codiga.plugins.jetbrains.annotators;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.GetRulesetsForClientQuery.Rule;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
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
    private long lastUpdatedTimeStamp;
    /**
     * -1 means the modification stamp of codiga.yml hasn't been set,
     * or there is no codiga.yml file in the project root.
     */
    private long configFileModificationStamp = -1;

    public RosieRulesCache(Project project) {
        this.cache = new ConcurrentHashMap<>();
    }

    public long getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    public void setLastUpdatedTimeStamp(long lastUpdatedTimeStamp) {
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }

    public boolean hasDifferentModificationStampThan(YAMLFile codigaConfigFile) {
        return codigaConfigFile.getModificationStamp() != configFileModificationStamp;
    }

    public void saveModificationStampOf(YAMLFile codigaConfigFile) {
        this.configFileModificationStamp = codigaConfigFile.getModificationStamp();
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
            .flatMap(ruleset -> ruleset.rules().stream())
            .collect(groupingBy(GetRulesetsForClientQuery.Rule::language))
            .entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, entry -> new RosieRulesCacheValue(toRosieRule(entry.getValue()))));
        //Clearing and repopulating the cache is easier than picking out one by one
        // the ones that remain, and the ones that have to be removed.
        cache.clear();
        cache.putAll(rulesByLanguage);
    }

    /**
     * Converts the argument list of GraphQL {@link Rule}s to {@link RosieRule}s.
     *
     * @param rules the rules to map
     */
    private List<RosieRule> toRosieRule(List<GetRulesetsForClientQuery.Rule> rules) {
        return rules.stream().map(RosieRule::new).collect(toList());
    }

    /**
     * Returns the list of {@code RosieRule}s for the argument language,
     * that will be sent to the Rosie service for analysis.
     *
     * @param language the language to return the rules for, or empty list if there is no rule cached for the
     *                 provided language
     */
    public List<RosieRule> getRulesForLanguage(LanguageEnumeration language) {
        var cachedValue = cache.get(language);
        return cachedValue != null ? cachedValue.getRules() : List.of();
    }

    /**
     * Empties the cache if it is not empty.
     */
    public void clear() {
        if (!cache.isEmpty()) {
            configFileModificationStamp = -1;
            cache.clear();
        }
    }

    @Override
    public void dispose() {
        clear();
    }

    public static RosieRulesCache getInstance(@NotNull Project project) {
        return project.getService(RosieRulesCache.class);
    }
}
