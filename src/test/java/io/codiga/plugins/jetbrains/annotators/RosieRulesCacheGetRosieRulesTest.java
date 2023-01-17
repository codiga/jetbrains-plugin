package io.codiga.plugins.jetbrains.annotators;

import static java.util.stream.Collectors.toList;

import com.intellij.psi.PsiFile;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.List;

/**
 * Integration test for {@link RosieRulesCache}.
 */
public class RosieRulesCacheGetRosieRulesTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosiecacherules";
    }

    public void testReturnsRulesForEmptyIgnoreConfig() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
            "  - python-ruleset");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testDoesntFiltersRulesForIgnoreConfigWithNoRuleset() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  ");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testDoesntFiltersRulesForIgnoreConfigWithNoRule() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesForIgnoreConfigWithNoPrefix() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
            "  - python-ruleset\n" +
            "ignore:\n" +
            "  - python-ruleset:\n" +
            "    - python_rule_2");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testFiltersRuleForIgnoreConfigWithOneMatchingPrefixWithLeadingSlash() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: /python");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithOneMatchingPrefixWithoutLeadingSlash() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: python");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithOneMatchingFilePathPrefix() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: /python_file.py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithOneMatchingDirectoryPathPrefix() {
        PsiFile psiFile = myFixture.configureByFile("directory/python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: /directory");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testDoestFilterRulesWithIgnoreConfigWithOnePrefixNotMatching() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: not-matching");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testDoesntFilterRulesWithIgnoreConfigWithOnePrefixContainingDoubleDots() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: python_file..py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithOneMatchingPrefixOfMultiple() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix:\n" +
                "        - not/matching\n" +
                "        - python_file.py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithMultipleMatchingPrefixes() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix:\n" +
                "        - /python\n" +
                "        - python_file.py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_1");
    }

    public void testDoesntFilterRulesWithIgnoreConfigWithMultiplePrefixesNotMatching() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix:\n" +
                "        - not-matching\n" +
                "        - also/not/matching");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testFiltersRulesWithIgnoreConfigWithMultipleRuleIgnoreConfigurations() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: python_file..py\n" +
                "    - python_rule_3:\n" +
                "      - prefix:\n" +
                "        - /python_fi");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            2,
            "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testDoesntFilterRulesWhenRuleDoesntBelongToRuleset() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - python-ruleset:\n" +
                "    - non_python_rule:\n" +
                "      - prefix: python_file..py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    public void testDoesntFilterRulesWhenRulesetIgnoreIsNotPresentInRulesetsProperty() {
        PsiFile psiFile = myFixture.configureByFile("python_file.py");
        var cache = configureCache(
            "rulesets:\n" +
                "  - python-ruleset\n" +
                "ignore:\n" +
                "  - not-configured-ruleset:\n" +
                "    - python_rule_2:\n" +
                "      - prefix: python_file.py");

        var rosieRules = cache.getRosieRules(LanguageEnumeration.PYTHON, psiFile.getVirtualFile().getPath());

        validateRuleCountAndRuleIds(rosieRules,
            3,
            "python-ruleset/python_rule_3", "python-ruleset/python_rule_2", "python-ruleset/python_rule_1");
    }

    //Helpers

    private RosieRulesCache configureCache(String codigaYmlText) {
        YAMLFile codigaFile = configureFile(codigaYmlText);

        var codigaYmlConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);
        var rulesets = CodigaApi.getInstance().getRulesetsForClient(codigaYmlConfig.getRulesets());
        var cache = RosieRulesCache.getInstance(getProject());
        cache.updateCacheFrom(rulesets.get());
        cache.setCodigaYmlConfig(codigaYmlConfig);

        return cache;
    }

    private YAMLFile configureFile(String text) {
        return (YAMLFile) myFixture.configureByText(YAMLFileType.YML, text);
    }

    private void validateRuleCountAndRuleIds(List<RosieRule> rules, int count, String... ruleIds) {
        assertSize(count, rules);

        var pythonRuleIds = rules.stream().map(rule -> rule.id).collect(toList());
        assertSameElements(pythonRuleIds, ruleIds);
    }
}
