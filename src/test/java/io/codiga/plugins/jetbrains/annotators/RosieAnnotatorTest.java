package io.codiga.plugins.jetbrains.annotators;

import io.codiga.plugins.jetbrains.testutils.TestBase;

/**
 * Integration test for {@link RosieAnnotator}.
 */
public class RosieAnnotatorTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosieannotator";
    }

    //Positive highlighting cases

    public void testNoHighlightForNoViolation() {
        doTestAnnotations("no_highlight_for_no_violation.py");
    }

    public void testHighlightForSingleViolation() {
        doTestAnnotations("highlight_for_single_violation.py", false, false, true);
    }

    public void testHighlightForMultipleViolations() {
        doTestAnnotations("highlight_for_multiple_violations.py", true, false, true);
    }

    //Negative highlighting cases

    public void testNoHighlightWhenFileRangeDoesntContainAnnotationStartOffset() {
        doTestAnnotations("no_highlight_for_start_offset_outside.py");
    }

    public void testNoHighlightWhenFileRangeDoesntContainAnnotationEndOffset() {
        doTestAnnotations("no_highlight_for_end_offset_outside.py");
    }

    //Rosie code quick fixes

    public void testTextInsertionFix() {
        doTestAnnotationFix("text_insertion_fix.py", "Fix: Insert text",
            "clasThis is the inserted texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextReplacementFixWithEditRangeMatchingViolationRange() {
        doTestAnnotationFix("text_replacement_fix_ranges_matching.py", "Fix: Replace text",
            "clasThis is the replacment textson:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextReplacementFixWithEditRangeNotMatchingViolationRange() {
        doTestAnnotationFix("text_replacement_fix_ranges_not_matching.py", "Fix: Replace text",
            "cThis is the replacement texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextRemovalFixWithEditRangeMatchingViolationRange() {
        doTestAnnotationFix("text_removal_fix_ranges_matching.py", "Fix: Remove text",
            "classon:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextRemovalFixWithEditRangeNotMatchingViolationRange() {
        doTestAnnotationFix("text_removal_fix_ranges_not_matching.py", "Fix: Remove text",
            "cs Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    //Disable Rosie analysis comment quick fixes

    public void testAddsCodigaDisableCommentToTopLevelElementInPython() {
        doTestAnnotationFix("add_top_level_disable_codiga.py", "Disable analysis for this row",
            "#codiga-disable\n" +
                "class PersonWithAddress:\n" +
                "  def __init__(self, name, age, address):\n" +
                "    self.name = name\n" +
                "    self.age = age\n" +
                "    self.address = address");
    }

    public void testAddsCodigaDisableCommentToNestedElementInPython() {
        doTestAnnotationFix("add_nested_disable_codiga.py", "Disable analysis for this row",
            "class PersonWithAddress:\n" +
                "  def __init__(self, name, age, long_address):\n" +
                "    self.name = name\n" +
                "    self.age = age\n" +
                "    #codiga-disable\n" +
                "    self.address = long_address");
    }

    public void testAddsCodigaDisableCommentToTopLevelElementInJava() {
        doTestAnnotationFix("add_top_level_disable_codiga.java", "Disable analysis for this row",
            "//codiga-disable\n" +
                "public class SomeClass {\n" +
                "}");
    }

    public void testAddsCodigaDisableCommentToNestedElementInJava() {
        doTestAnnotationFix("add_nested_disable_codiga.java", "Disable analysis for this row",
            "public class SomeClass {\n" +
                "    void method() {\n" +
                "        boolean isPassing = true;\n" +
                "        if (isPassing)\n" +
                "            //codiga-disable\n" +
                "            String passing = \"passing\";\n" +
                "    }\n" +
                "}");
    }

    //Helpers

    /**
     * Note: there is an issue on platform-level with using info-level checks,
     * when there are actual annotations in the file. That is why it is turned off in some tests.
     * <p>
     * Note 2: info-level checks is enabled for no-highlight test cases to completely make sure that no Rosie related
     * annotations are added in the test file.
     */
    private void doTestAnnotations(String filePath, boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings) {
        myFixture.configureByFile(filePath);
        myFixture.testHighlighting(checkWarnings, checkInfos, checkWeakWarnings);
    }

    private void doTestAnnotations(String filePath) {
        doTestAnnotations(filePath, true, true, true);
    }

    private void doTestAnnotationFix(String filePath, String fixName, String afterText) {
        myFixture.configureByFile(filePath);
        myFixture.doHighlighting();
        myFixture.launchAction(myFixture.findSingleIntention(fixName));
        myFixture.checkResult(afterText);
    }
}
