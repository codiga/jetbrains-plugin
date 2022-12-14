package io.codiga.plugins.jetbrains.annotators;

import io.codiga.plugins.jetbrains.testutils.TestBase;

/**
 * Integration test for {@link DisableRosieAnalysisFix}.
 */
public class DisableRosieAnalysisFixTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosieannotator";
    }

    public void testAddsCodigaDisableCommentToTopLevelElementInPython() {
        doTestAnnotationFix("add_top_level_disable_codiga.py", "Remove error 'single_rule'",
            "# codiga-disable\n" +
                "class PersonWithAddress:\n" +
                "  def __init__(self, name, age, address):\n" +
                "    self.name = name\n" +
                "    self.age = age\n" +
                "    self.address = address");
    }

    public void testAddsCodigaDisableCommentToNestedElementInPython() {
        doTestAnnotationFix("add_nested_disable_codiga.py", "Remove error 'single_rule'",
            "class PersonWithAddress:\n" +
                "  def __init__(self, name, age, long_address):\n" +
                "    self.name = name\n" +
                "    self.age = age\n" +
                "    # codiga-disable\n" +
                "    self.address = long_address");
    }

    public void testAddsCodigaDisableCommentToTopLevelElementInJava() {
        doTestAnnotationFix("add_top_level_disable_codiga.java", "Remove error 'single_rule'",
            "// codiga-disable\n" +
                "public class SomeClass {\n" +
                "}");
    }

    public void testAddsCodigaDisableCommentToNestedElementInJava() {
        doTestAnnotationFix("add_nested_disable_codiga.java", "Remove error 'single_rule'",
            "public class SomeClass {\n" +
                "    void method() {\n" +
                "        boolean isPassing = true;\n" +
                "        if (isPassing)\n" +
                "            // codiga-disable\n" +
                "            String passing = \"passing\";\n" +
                "    }\n" +
                "}");
    }

    private void doTestAnnotationFix(String filePath, String fixName, String afterText) {
        myFixture.configureByFile(filePath);
        myFixture.doHighlighting();
        myFixture.launchAction(myFixture.findSingleIntention(fixName));
        myFixture.checkResult(afterText);
    }
}