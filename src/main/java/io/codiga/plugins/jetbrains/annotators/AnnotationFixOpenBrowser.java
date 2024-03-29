package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Fix to open the browser to learn more about a violation and a rule.
 */
@RequiredArgsConstructor
public class AnnotationFixOpenBrowser extends RosieAnnotationIntentionBase {

    /**
     * https://app.codiga.io/hub/ruleset/&lt;ruleset-name>/&lt;rule-name>
     */
    private static final String RULE_DETAILS_URL = "https://app.codiga.io/hub/ruleset/%s/%s";
    private final RosieAnnotationJetBrains rosieAnnotation;

    @Override
    public @IntentionName @NotNull String getText() {
        return String.format("See rule '%s' on the Codiga Hub", rosieAnnotation.getRuleName());
    }

    /**
     * Opens the rule's page on Codiga Hub for this particular violation.
     */
    @Override
    public void doInvoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        try {
            BrowserUtil.browse(getUrlString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TestOnly
    String getUrlString() {
        return String.format(RULE_DETAILS_URL, rosieAnnotation.getRulesetName(), rosieAnnotation.getRuleName());
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
