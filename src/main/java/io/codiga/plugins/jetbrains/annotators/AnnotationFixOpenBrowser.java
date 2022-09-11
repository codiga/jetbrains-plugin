package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URL;

/**
 * Fix to open the browser to learn more about a violation and a rule.
 */
public class AnnotationFixOpenBrowser implements IntentionAction {

    private RosieAnnotationJetBrains rosieAnnotation;

    public AnnotationFixOpenBrowser(RosieAnnotationJetBrains rosieAnnotation) {
        this.rosieAnnotation = rosieAnnotation;
    }

    @Override
    public @IntentionName
    @NotNull String getText() {
        return "See on Codiga Hub: rate or comment";
    }

    @Override
    public @NotNull
    @IntentionFamilyName String getFamilyName() {
        return "Codiga";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    /**
     * Open Codiga that contains the file for this particular violation.
     *
     * @param project
     * @param editor
     * @param file
     * @throws IncorrectOperationException
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        try {
            String urlString = String.format("https://app.codiga.io/hub");
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
