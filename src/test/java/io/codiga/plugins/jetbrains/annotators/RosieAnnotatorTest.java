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
}
