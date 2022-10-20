package io.codiga.plugins.jetbrains.annotators;

import static java.util.stream.Collectors.toList;

import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import io.codiga.plugins.jetbrains.testutils.TestBase;

import java.util.List;

/**
 * Integration test for {@link RosieRulesCache}.
 */
public class RosieRulesCacheTest extends TestBase {

    public void testStoresRulesFromSingleRulesetForASingleLanguageInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("singleRulesetSingleLanguage");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 3, "10", "11", "12");
    }

    public void testStoresRulesFromSingleRulesetForAMultipleLanguagesInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("singleRulesetMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 2, "10", "12");
        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.JAVA), 1, "20");
    }

    public void testStoresRulesFromMultipleRulesetsForSingleLanguageGroupedByLanguageInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsSingleLanguage");

        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);
        assertContainsOrdered(
            rulesets.get().stream().map(GetRulesetsForClientQuery.RuleSetsForClient::name).collect(toList()),
            "python-ruleset", "python-ruleset-2");

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 3, "10", "11", "12");
    }

    public void testStoresRulesFromMultipleRulesetsForMultipleLanguagesGroupedByLanguageInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsMultipleLanguages");

        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);
        assertContainsOrdered(
            rulesets.get().stream().map(GetRulesetsForClientQuery.RuleSetsForClient::name).collect(toList()),
            "mixed-ruleset", "python-ruleset");

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 2, "30", "31");
        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.JAVA), 1, "20");
    }

    public void testOverridesCacheWithNewRules() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        //Initial cache population

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 2, "30", "31");
        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.JAVA), 1, "20");

        //Cache refresh

        var rulesetNamesNew = List.of("singleRulesetSingleLanguage");
        var rulesetsNew = CodigaApi.getInstance().getRulesetsForClient(rulesetNamesNew);

        cache.updateCacheFrom(rulesetsNew.get());

        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.PYTHON), 3, "10", "11", "12");
        validateRuleCountAndRuleIds(cache.getRulesForLanguage(LanguageEnumeration.JAVA), 0);
    }

    public void testReturnsRulesForLanguage() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("singleRulesetMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        var pythonRules = cache.getRulesForLanguage(LanguageEnumeration.PYTHON);
        assertTrue(pythonRules.stream().allMatch(rule -> LanguageEnumeration.PYTHON.rawValue().equals(rule.language)));

        var javaRules = cache.getRulesForLanguage(LanguageEnumeration.JAVA);
        assertTrue(javaRules.stream().allMatch(rule -> LanguageEnumeration.JAVA.rawValue().equals(rule.language)));
    }

    //Helpers

    private void validateRuleCountAndRuleIds(List<RosieRule> rules, int count, String... ruleIds) {
        assertSize(count, rules);

        var pythonRuleIds = rules.stream().map(rule -> rule.id).collect(toList());
        assertSameElements(pythonRuleIds, ruleIds);
    }
}
