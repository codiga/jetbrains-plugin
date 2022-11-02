package io.codiga.plugins.jetbrains.rosie;

import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * Integration test for {@link CodigaConfigFileUtil}.
 */
public class CodigaConfigFileUtilFindConfigTest extends TestBase {
    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/codigaconfig";
    }

    //Positive cases

    public void testReturnsConfigFile() {
        myFixture.copyFileToProject("codiga.yml");

        YAMLFile codigaConfigFile = CodigaConfigFileUtil.findCodigaConfigFile(getProject());

        assertNotNull(codigaConfigFile);
        assertEquals("rulesets:\n  - ruleset-name\n", codigaConfigFile.getText());
    }

    //Negative cases

    public void testReturnsNoFileForNoProject() {
        YAMLFile codigaConfigFile = CodigaConfigFileUtil.findCodigaConfigFile(null);

        assertNull(codigaConfigFile);
    }

    public void testResultsNoFileForMissingConfigFile() {
        YAMLFile codigaConfigFile = CodigaConfigFileUtil.findCodigaConfigFile(getProject());

        assertNull(codigaConfigFile);
    }
}
