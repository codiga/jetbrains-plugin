package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.ImaginaryEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Rosie annotator related intention actions.
 */
public abstract class RosieAnnotationIntentionBase implements IntentionAction {

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Codiga";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Imaginary editors, like {@link com.intellij.codeInsight.intention.impl.preview.IntentionPreviewEditor},
     * are excluded, there the intention is not invoked.
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (editor instanceof ImaginaryEditor)
            return;

        doInvoke(project, editor, psiFile);
    }

    /**
     * Performs the actual intention action logic.
     */
    protected abstract void doInvoke(@NotNull Project project, Editor editor, PsiFile psiFile);
}
