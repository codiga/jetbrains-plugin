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

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            3,
            "python-ruleset/python_rule_1", "python-ruleset/python_rule_2", "python-ruleset/python_rule_3");
    }

    public void testStoresRulesFromSingleRulesetForAMultipleLanguagesInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("singleRulesetMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            2,
            "mixed-ruleset/python_rule_1", "mixed-ruleset/python_rule_3");
        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.JAVA),
            1,
            "mixed-ruleset/java_rule_1");
    }

    public void testStoresRulesFromMultipleRulesetsForSingleLanguageGroupedByLanguageInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsSingleLanguage");

        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);
        assertContainsOrdered(
            rulesets.get().stream().map(GetRulesetsForClientQuery.RuleSetsForClient::name).collect(toList()),
            "python-ruleset", "python-ruleset-2");

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            3,
            "python-ruleset/python_rule_2", "python-ruleset-2/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testStoresRulesFromMultipleRulesetsForMultipleLanguagesGroupedByLanguageInEmptyCache() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsMultipleLanguages");

        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);
        assertContainsOrdered(
            rulesets.get().stream().map(GetRulesetsForClientQuery.RuleSetsForClient::name).collect(toList()),
            "mixed-ruleset", "python-ruleset");

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            2,
            "python-ruleset/python_rule_5", "mixed-ruleset/python_rule_4");
        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.JAVA),
            1,
            "mixed-ruleset/java_rule_1");
    }

    public void testOverridesCacheWithNewRules() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("multipleRulesetsMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        //Initial cache population

        cache.updateCacheFrom(rulesets.get());

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            2,
            "python-ruleset/python_rule_5", "mixed-ruleset/python_rule_4");
        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.JAVA),
            1,
            "mixed-ruleset/java_rule_1");

        //Cache refresh

        var rulesetNamesNew = List.of("singleRulesetSingleLanguage");
        var rulesetsNew = CodigaApi.getInstance().getRulesetsForClient(rulesetNamesNew);

        cache.updateCacheFrom(rulesetsNew.get());

        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON),
            3,
            "python-ruleset/python_rule_1", "python-ruleset/python_rule_2", "python-ruleset/python_rule_3");
        validateRuleCountAndRuleIds(cache.getRosieRulesForLanguage(LanguageEnumeration.JAVA), 0);
    }

    public void testReturnsRulesForLanguage() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("singleRulesetMultipleLanguages");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        var pythonRules = cache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON);
        assertTrue(pythonRules.stream().allMatch(rule -> LanguageEnumeration.PYTHON.rawValue().equals(rule.language)));

        var javaRules = cache.getRosieRulesForLanguage(LanguageEnumeration.JAVA);
        assertTrue(javaRules.stream().allMatch(rule -> LanguageEnumeration.JAVA.rawValue().equals(rule.language)));
    }

    public void testReturnsJavaScriptRulesForTypeScript() {
        var cache = RosieRulesCache.getInstance(getProject());
        var rulesetNames = List.of("javascriptRuleset");
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(rulesetNames);

        cache.updateCacheFrom(rulesets.get());

        var jsRules = cache.getRosieRulesForLanguage(LanguageEnumeration.TYPESCRIPT);
        assertTrue(jsRules.stream().allMatch(rule -> LanguageEnumeration.JAVASCRIPT.rawValue().equals(rule.language)));
    }

    //Helpers

    private void validateRuleCountAndRuleIds(List<RosieRule> rules, int count, String... ruleIds) {
        assertSize(count, rules);

        var pythonRuleIds = rules.stream().map(rule -> rule.id).collect(toList());
        assertSameElements(pythonRuleIds, ruleIds);
    }
}
