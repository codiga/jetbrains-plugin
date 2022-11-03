package io.codiga.plugins.jetbrains.rosie;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.List;

/**
 * Integration test for {@link CodigaConfigFileUtil}.
 */
public class CodigaConfigFileUtilGetRulesetsTest extends TestBase {

    //Positive cases

    public void testReturnsNonEmptyRulesetNames() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of("my-python-ruleset", "my-other-ruleset"), rulesetNames);
    }

    public void testReturnsNoRulesetNameForEmptyRulesetsList() {
        YAMLFile codigaFile = configureFile("rulesets:\n  - ");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of(), rulesetNames);
    }

    public void testReturnsFilteredRulesetNamesWhenThereIsEmptyListItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset\n" +
                "  - \n" +
                "  - some-ruleset");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of("my-python-ruleset", "my-other-ruleset", "some-ruleset"), rulesetNames);
    }

    public void testReturnsFilteredRulesetNamesWhenThereIsNonPlainTextListItem() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - rules:\n" +
                "      - some-rule\n" +
                "  - my-other-ruleset");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of("my-python-ruleset", "my-other-ruleset"), rulesetNames);
    }

    //Negative cases

    public void testReturnsNoRulesetNamesForMissingRulesetsMapping() {
        YAMLFile codigaFile = configureFile(
            "not-rulesets:\n" +
                "  - my-python-ruleset\n" +
                "  - my-other-ruleset");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of(), rulesetNames);
    }

    public void testReturnsNoRulesetNameForNonSequenceEmptyRulesetsList() {
        YAMLFile codigaFile = configureFile("rulesets:\n  ");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of(), rulesetNames);
    }

    public void testReturnsNoRulesetNamesForNonSequenceRulesetsMapping() {
        YAMLFile codigaFile = configureFile(
            "rulesets:\n" +
                "  rules:");

        List<String> rulesetNames = CodigaConfigFileUtil.collectRulesetNames(codigaFile);

        assertIterableEquals(List.of(), rulesetNames);
    }

    private YAMLFile configureFile(String text) {
        return (YAMLFile) myFixture.configureByText(YAMLFileType.YML, text);
    }
}
