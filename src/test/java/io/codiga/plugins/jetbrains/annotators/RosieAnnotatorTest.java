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

    //Positive cases

    public void testNoHighlightForNoViolation() {
        doTestAnnotations("no_highlight_for_no_violation.py");
    }

    public void testHighlightForSingleViolation() {
        doTestAnnotations("highlight_for_single_violation.py", false, false, true);
    }

    public void testHighlightForMultipleViolations() {
        doTestAnnotations("highlight_for_multiple_violations.py", true, false, true);
    }

    public void testTextInsertionFix() {
        doTestAnnotationFixes("text_insertion_fix.py", "Fix: Insert text",
            "clasThis is the inserted texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextReplacementFixWithEditRangeMatchingViolationRange() {
        doTestAnnotationFixes("text_replacement_fix_ranges_matching.py", "Fix: Replace text",
            "clasThis is the replacment textson:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextReplacementFixWithEditRangeNotMatchingViolationRange() {
        doTestAnnotationFixes("text_replacement_fix_ranges_not_matching.py", "Fix: Replace text",
            "cThis is the replacement texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextRemovalFixWithEditRangeMatchingViolationRange() {
        doTestAnnotationFixes("text_removal_fix_ranges_matching.py", "Fix: Remove text",
            "classon:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextRemovalFixWithEditRangeNotMatchingViolationRange() {
        doTestAnnotationFixes("text_removal_fix_ranges_not_matching.py", "Fix: Remove text",
            "cs Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    //Negative cases

    public void testNoHighlightWhenFileRangeDoesntContainAnnotationStartOffset() {
        doTestAnnotations("no_highlight_for_start_offset_outside.py");
    }

    public void testNoHighlightWhenFileRangeDoesntContainAnnotationEndOffset() {
        doTestAnnotations("no_highlight_for_end_offset_outside.py");
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

    private void doTestAnnotationFixes(String filePath, String fixName, String afterText) {
        myFixture.configureByFile(filePath);
        myFixture.doHighlighting();
        myFixture.launchAction(myFixture.findSingleIntention(fixName));
        myFixture.checkResult(afterText);
    }
}
