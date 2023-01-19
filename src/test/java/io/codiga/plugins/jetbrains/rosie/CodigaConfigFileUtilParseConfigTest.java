package io.codiga.plugins.jetbrains.rosie;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.codiga.plugins.jetbrains.rosie.model.codiga.CodigaYmlConfig;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.List;

public class CodigaConfigFileUtilParseConfigTest extends TestBase {

    //rulesets configuration

    public void testReturnsNonEmptyRulesetNames() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertIterableEquals(List.of("my-python-ruleset", "my-other-ruleset"), codigaConfig.getRulesets());
        assertEquals(0, codigaConfig.getIgnore().size());
    }

    public void testReturnsNoRulesetNameForEmptyRulesetsList() {
        YAMLFile codigaFile = configureFile("rulesets:\n  - ");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of(), codigaConfig.getRulesets());
    }

    public void testReturnsFilteredRulesetNamesWhenThereIsEmptyListItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "  - \n" +
                "  - some-ruleset");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of("my-python-ruleset", "my-other-ruleset", "some-ruleset"), codigaConfig.getRulesets());
    }

    public void testReturnsNoRulesetNameWhenThereIsNonPlainTextListItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - rules:\n" +
                "      - some-rule\n" +
                "  - my-other-ruleset");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of(), codigaConfig.getRulesets());
    }

    public void testReturnsNoRulesetNamesForMissingRulesetsMapping() {
        YAMLFile codigaFile = configureFile(
            "not-rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of(), codigaConfig.getRulesets());
    }

    public void testReturnsNoRulesetNameForNonSequenceEmptyRulesetsList() {
        YAMLFile codigaFile = configureFile("rulesets:\n  ");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of(), codigaConfig.getRulesets());
    }

    public void testReturnsNoRulesetNamesForNonSequenceRulesetsMapping() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  rules:");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertIterableEquals(List.of(), codigaConfig.getRulesets());
    }

    //ignore configuration

    public void testReturnsEmptyIgnoreConfigForNonPropertyIgnore() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertEquals(codigaConfig, CodigaYmlConfig.EMPTY);
    }

    public void testReturnsIgnoreConfigForNoIgnoreItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(0, codigaConfig.getIgnore().size());
    }

    public void testReturnsIgnoreConfigForBlankIgnoreItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - ");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(0, codigaConfig.getIgnore().size());
    }

    public void testReturnsEmptyIgnoreConfigForStringIgnoreItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertEquals(codigaConfig, CodigaYmlConfig.EMPTY);
    }

    public void testReturnsIgnoreConfigForEmptyRulesetNameIgnoreProperty() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
    }

    public void testReturnsIgnoreConfigForStringRuleNameIgnoreProperty() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigForEmptyRuleNameIgnoreProperty() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigForStringPrefixIgnoreProperty() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigForEmptyPrefixIgnoreProperty() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigForBlankPrefix() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:     ");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigForSinglePrefix() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix: /path/to/file/to/ignore");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path/to/file/to/ignore", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
    }

    public void testReturnsIgnoreConfigForSinglePrefixAsList() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path/to/file/to/ignore");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path/to/file/to/ignore", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
    }

    public void testReturnsIgnoreConfigForMultiplePrefixesAsList() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path2");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
        assertEquals("/path2", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(1));
    }

    public void testReturnsIgnoreConfigForMultipleRuleIgnores() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path2\n" +
                "    - rule2");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
        assertEquals("/path2", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(1));
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule2").getPrefixes().size());
    }

    public void testReturnsIgnoreConfigWithoutRulesets() {
        YAMLFile codigaFile = configureFile(
            "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path2");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
        assertEquals("/path2", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(1));
    }

    public void testReturnsIgnoreConfigForDuplicatePrefixProperties() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path2\n" +
                "      - prefix: /path3");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(3, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
        assertEquals("/path2", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(1));
        assertEquals("/path3", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(2));
    }

    public void testReturnsIgnoreConfigForDuplicatePrefixValues() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path1");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(1, codigaConfig.getIgnore().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(1, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
    }

    public void testReturnsIgnoreConfigForMultipleRulesetIgnores() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "ignore:\n" +
                "  - my-python-ruleset:\n" +
                "    - rule1:\n" +
                "      - prefix:\n" +
                "        - /path1\n" +
                "        - /path2\n" +
                "    - rule2\n" +
                "  - my-other-ruleset:\n" +
                "    - rule3:\n" +
                "      - prefix: /another/path");

        var codigaConfig = CodigaConfigFileUtil.parseCodigaYml(codigaFile);

        assertNotEquals(CodigaYmlConfig.EMPTY, codigaConfig);
        assertEquals(2, codigaConfig.getIgnore().size());

        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnores().size());
        assertEquals(2, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().size());
        assertEquals("/path1", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(0));
        assertEquals("/path2", codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule1").getPrefixes().get(1));
        assertEquals(0, codigaConfig.getIgnore("my-python-ruleset").getRuleIgnore("rule2").getPrefixes().size());

        assertEquals(1, codigaConfig.getIgnore("my-other-ruleset").getRuleIgnores().size());
        assertEquals(1, codigaConfig.getIgnore("my-other-ruleset").getRuleIgnore("rule3").getPrefixes().size());
        assertEquals("/another/path", codigaConfig.getIgnore("my-other-ruleset").getRuleIgnore("rule3").getPrefixes().get(0));
    }

    private YAMLFile configureFile(String text) {
        return (YAMLFile) myFixture.configureByText(YAMLFileType.YML, text);
    }
}
