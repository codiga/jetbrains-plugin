package io.codiga.plugins.jetbrains.rosie;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for {@link CodigaConfigFileUtil}.
 */
@RunWith(Parameterized.class)
public class CodigaConfigFileUtilInvalidRulesetNamesTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            //Invalid content
            { "   ", false},
            { "\n", false},
            { "-", false},
            { "- ", false},
            { "-ruleset-name", false},
            { "ruleset_name", false},
            { "AwEsom3Stuff!`", false},
            { "ruleset%67", false},
            { "ruleset name", false},
            { "ruleset-with-german-ß-in-it", false},

            //Invalid length
            { "", false},
            { "r", false},
            { "ru", false},
            { "rul", false},
            { "rule", false},

            //Valid cases
            { "5long", true},
            { "123456789", true},
            { "python-ruleset-63", true},
            { "python-ruleset-name", true},
        });
    }

    @Parameterized.Parameter(0)
    public String rulesetName;
    @Parameterized.Parameter(1)
    public boolean isValid;

    @Test
    public void shouldValidateRulesetNames() {
        assertEquals(CodigaConfigFileUtil.isRulesetNameValid(rulesetName), isValid);
    }
}
