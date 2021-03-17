package com.code_inspector.plugins.intellij.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URL;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.ui.UIConstants.ANNOTATION_FIX_OPEN_BROWSER;

/**
 * This class implements a quick fix to see a violation directly in the browser. It opens the default
 * browser and show Code Inspector results for this violation.
 */
public class CodeInspectionAnnotationFixOpenBrowser implements IntentionAction {

    private final Long projectId;
    private final Long analysisId;
    private final String filename;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public CodeInspectionAnnotationFixOpenBrowser(Long _projectId, Long _analysisId, String _filename) {
        this.projectId = _projectId;
        this.analysisId = _analysisId;
        this.filename = _filename;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return ANNOTATION_FIX_OPEN_BROWSER;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Code Inspector";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        LOGGER.debug("CodeInspectionAnnotationFixOpenBrowser - calling isAvailable()");
        return true;
    }

    /**
     * Open Code Inspector that contains the file for this particular violation.
     * @param project
     * @param editor
     * @param file
     * @throws IncorrectOperationException
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        LOGGER.debug("CodeInspectionAnnotationFixOpenBrowser - calling invoke()");
        try {
            String urlString = String.format("https://frontend.code-inspector.com/analysis/result/%s/%s?file=%s", projectId, analysisId, filename);
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception e) {
            LOGGER.debug("CodeInspectionAnnotationFixOpenBrowser - cannot open browser");
            e.printStackTrace();
        }
    }

    @Override
    public boolean startInWriteAction() {
        LOGGER.debug("CodeInspectionAnnotationFixOpenBrowser - calling startInWriteAction()");
        return true;
    }
}
