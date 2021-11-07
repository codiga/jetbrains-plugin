package io.codiga.plugins.jetbrains.annotators;

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

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.ui.UIConstants.ANNOTATION_FIX_URL_LEARN_MORE;

/**
 * This class implements a quick fix to open the url of a violation so that the developer can learn more
 * about a particular violation.
 */
public class AnnotationFixLearnMore implements IntentionAction {

    private final String url;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public AnnotationFixLearnMore(String _url) {
        this.url = _url;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return ANNOTATION_FIX_URL_LEARN_MORE;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Codiga";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        LOGGER.debug("CodeInspectionAnnotationFixLearnMore - calling isAvailable()");
        return true;
    }

    /**
     * Invoke the fix, and open the URL from the constructor in the browser
     * @param project
     * @param editor
     * @param file
     * @throws IncorrectOperationException
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        LOGGER.debug("CodeInspectionAnnotationFixLearnMore - calling invoke()");
        try {
            Desktop.getDesktop().browse(new URL(this.url).toURI());
        } catch (Exception e) {
            LOGGER.debug("CodeInspectionAnnotationFixLearnMore - cannot open browser");
            e.printStackTrace();
        }
    }

    @Override
    public boolean startInWriteAction() {
        LOGGER.debug("CodeInspectionAnnotationFixLearnMore - calling startInWriteAction()");
        return true;
    }
}
