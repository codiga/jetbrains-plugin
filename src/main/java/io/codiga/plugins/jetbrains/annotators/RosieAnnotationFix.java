package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFix;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFixEdit;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

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
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        try {
            WriteCommandAction.writeCommandAction(project).run(
                (ThrowableRunnable<Throwable>) () -> {
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
            );
        } catch (Throwable e) {
            LOGGER.error("cannot add in editor", e);
        }
    }
}
