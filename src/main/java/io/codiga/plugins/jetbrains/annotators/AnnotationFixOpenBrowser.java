package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import org.jetbrains.annotations.NotNull;

/**
 * Fix to open the browser to learn more about a violation and a rule.
 */
public class AnnotationFixOpenBrowser implements IntentionAction {

    /**
     * https://app.codiga.io/hub/ruleset/&lt;ruleset-name>/&lt;rule-name>
     */
    private static final String RULE_DETAILS_URL = "https://app.codiga.io/hub/ruleset/%s/%s";
    private final RosieAnnotationJetBrains rosieAnnotation;

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
     * Opens the rule's page on Codiga Hub for this particular violation.
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        try {
            String urlString = String.format(RULE_DETAILS_URL, rosieAnnotation.getRulesetName(), rosieAnnotation.getRuleName());
            BrowserUtil.browse(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
