package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFix;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFixEdit;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.model.rosie.RosieConstants.*;

/**
 * This is an Intention Action to apply a fix with the series of edits on the code.
 * <p>
 * It is used and instantiated by {@link RosieAnnotator} via {@link com.intellij.lang.annotation.AnnotationBuilder}.
 */
@RequiredArgsConstructor
public class RosieAnnotationFix extends RosieAnnotationIntentionBase {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final RosieViolationFix rosieViolationFix;

    @Override
    public @IntentionName @NotNull String getText() {
        return String.format("Fix: %s", rosieViolationFix.description);
    }

    @Override
    public void doInvoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        try {
            WriteCommandAction.writeCommandAction(project).run(
                (ThrowableRunnable<Throwable>) () -> {
                    if (!hasInvalidEditOffset(project, editor, psiFile)) {
                        Document document = editor.getDocument();
                        for (RosieViolationFixEdit edit : this.rosieViolationFix.edits) {
                            LOGGER.info(String.format("Applying fix %s content |%s|", edit.editType, edit.content));
                            if (edit.editType.equalsIgnoreCase(ROSIE_FIX_ADD)) {
                                document.insertString(edit.start.getOffset(editor), edit.content);
                            }
                            if (edit.editType.equalsIgnoreCase(ROSIE_FIX_UPDATE)) {
                                document.replaceString(edit.start.getOffset(editor), edit.end.getOffset(editor), edit.content);
                            }
                            if (edit.editType.equalsIgnoreCase(ROSIE_FIX_REMOVE)) {
                                document.deleteString(edit.start.getOffset(editor), edit.end.getOffset(editor));
                            }
                        }
                    }
                }
            );
        } catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
            //Fall through, don't have to log anything. Showing the error hint in the editor is enough.
        } catch (Throwable e) {
            LOGGER.error("Cannot apply fix in editor.", e);
        }
    }

    private void recordRuleFix() {
        try {
            CodigaApi.getInstance().recordRuleFix();
        } catch (Exception e) {
            //Even if recording this metric fails, the application of the fix should be performed
        }
    }

    /**
     * If the start offset for additions, or the start/end offset for removals and updates, received from the rule configuration,
     * is either null or is outside the current file's range, we show an error, and don't apply the fix.
     */
    @VisibleForTesting
    boolean hasInvalidEditOffset(@NotNull Project project, Editor editor, PsiFile psiFile) {
        boolean hasInvalidOffset = true;
        try {
            TextRange fileRange = psiFile.getTextRange();
            hasInvalidOffset = this.rosieViolationFix.edits
                .stream()
                .anyMatch(edit -> {
                    if (edit.editType.equalsIgnoreCase(ROSIE_FIX_ADD)) {
                        return edit.start == null || !fileRange.contains(edit.start.getOffset(editor));
                    }
                    return edit.start == null || edit.end == null
                        || !fileRange.contains(edit.start.getOffset(editor))
                        || !fileRange.contains(edit.end.getOffset(editor));
                });
        } catch (IndexOutOfBoundsException e) {
            //Let it through, so that it can be handled by the 'hasInvalidOffset' check below.
        }

        if (hasInvalidOffset) {
            CommonRefactoringUtil.showErrorHint(project, editor,
                "Can't apply the fix due to invalid start/end offsets.",
                "Can't Apply Fix",
                null);
        }
        return hasInvalidOffset;
    }
}
