package io.codiga.plugins.jetbrains.annotators;

import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import io.codiga.plugins.jetbrains.model.rosie.RosiePosition;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFix;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFixEdit;
import io.codiga.plugins.jetbrains.testutils.TestBase;

import java.util.List;

/**
 * Integration test for {@link RosieAnnotationFix}.
 */
public class RosieAnnotationFixTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosieannotator";
    }

    //Insert

    public void testTextInsertionFix() {
        doTestAnnotationFix("text_insertion_fix.py", "Fix: Insert text",
            "clasThis is the inserted texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    public void testTextInsertionFixForNullEnd() {
        doTestAnnotationFix("text_insertion_fix_null_end.py", "Fix: Insert text null end",
            "clasThis is the inserted texts Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
    }

    //Replace

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

    //Delete

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

    //Negative cases

    public void testDoesntApplyFixWhenStartIsNullForAddition() {
        PsiFile psiFile = myFixture.configureByFile("text_insertion_fix.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        null,
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenStartIsNullForNonAddition() {
        PsiFile psiFile = myFixture.configureByFile("text_replacement_fix_ranges_matching.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        null,
                        new RosiePosition(3, 10),
                        "Content",
                        "update"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 10),
                        "Content",
                        "update"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenEndIsNullForNonAddition() {
        PsiFile psiFile = myFixture.configureByFile("text_replacement_fix_ranges_matching.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        null,
                        "Content",
                        "update"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 10),
                        "Content",
                        "update"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenStartLineIsOutOfRange() {
        PsiFile psiFile = myFixture.configureByFile("text_insertion_fix.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        new RosiePosition(6, 5), //start line is invalid
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenStartColumnIsOutOfRange() {
        PsiFile psiFile = myFixture.configureByFile("text_insertion_fix.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        new RosiePosition(1, -1), //start column is invalid
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 10),
                        "Content",
                        "add"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenEndLineIsOutOfRange() {
        PsiFile psiFile = myFixture.configureByFile("text_replacement_fix_ranges_matching.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 1),
                        new RosiePosition(3, 10),
                        "Content",
                        "update"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(6, 10), //end line is invalid
                        "Content",
                        "update"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    public void testDoesntApplyFixWhenEndColumnIsOutOfRange() {
        PsiFile psiFile = myFixture.configureByFile("text_replacement_fix_ranges_matching.py");
        var fix = new RosieAnnotationFix(
            new RosieViolationFix(
                "Apply fix",
                List.of(
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 1),
                        new RosiePosition(3, 10),
                        "Content",
                        "update"
                    ),
                    new RosieViolationFixEdit(
                        new RosiePosition(1, 5),
                        new RosiePosition(3, 100), //end column is invalid
                        "Content",
                        "update"
                    ))));

        assertThrows(
            CommonRefactoringUtil.RefactoringErrorHintException.class,
            () -> fix.hasInvalidEditOffset(getProject(), myFixture.getEditor(), psiFile));
    }

    private void doTestAnnotationFix(String filePath, String fixName, String afterText) {
        myFixture.configureByFile(filePath);
        myFixture.doHighlighting();
        myFixture.launchAction(myFixture.findSingleIntention(fixName));
        myFixture.checkResult(afterText);
    }
}
