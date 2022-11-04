package io.codiga.plugins.jetbrains.annotators;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.LanguageUtils.commentPrefixFor;

import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtilEx;
import com.intellij.openapi.editor.actions.StartNewLineBeforeAction;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Adds the {@code codiga-disable} string as a comment above the line on which this intention is invoked.
 * <p>
 * This will make the Rosie service ignore that line during analysis.
 * <p>
 * NOTE: the correct indentation is applied only when the IDE itself recognizes the language of the file,
 * otherwise the comment is added at the beginning of the new line.
 */
@RequiredArgsConstructor
public class DisableRosieAnalysisFix extends RosieAnnotationIntentionBase {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final String CODIGA_DISABLE = "codiga-disable";
    private final String ruleName;

    @Override
    public @IntentionName @NotNull String getText() {
        return String.format("Remove error '%s'", ruleName);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        AnAction action = ActionManager.getInstance().getAction("EditorStartNewLineBefore");
        if (!(action instanceof StartNewLineBeforeAction)) {
            //This shouldn't happen since the action is available on platform-level
            LOGGER.warn("'codiga-disable' comment cannot be added since the 'EditorStartNewLineBefore' action is not available.");
            return;
        }

        //Insert a new line before the current one. It places the caret to a position with correct indentation.
        var startNewLineBeforeAction = (StartNewLineBeforeAction) action;
        startNewLineBeforeAction.actionPerformed(editor, EditorUtil.getEditorDataContext(editor));

        //Create the comment with the prefix associated with one-line comments in the file's language.
        var language = LanguageUtils.getLanguageFromFilename(psiFile.getName());
        String codigaDisableComment = commentPrefixFor(language) + " " + CODIGA_DISABLE;

        //Insert the comment text
        EditorModificationUtilEx.insertStringAtCaret(editor, codigaDisableComment);
    }
}
