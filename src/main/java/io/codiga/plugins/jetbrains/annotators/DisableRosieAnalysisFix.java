package io.codiga.plugins.jetbrains.annotators;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.StartNewLineBeforeAction;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.codiga.plugins.jetbrains.utils.LanguageUtils;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
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
    private final RosieAnnotationJetBrains annotation;

    @Override
    public @IntentionName @NotNull String getText() {
        return String.format("Remove error '%s'", ruleName);
    }

    @Override
    public void doInvoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        AnAction action = ActionManager.getInstance().getAction("EditorStartNewLineBefore");
        if (!(action instanceof StartNewLineBeforeAction)) {
            //This shouldn't happen since the action is available on platform-level
            LOGGER.warn("'codiga-disable' comment cannot be added since the 'EditorStartNewLineBefore' action is not available.");
            return;
        }

        Document document = editor.getDocument();
        int lineAtViolationStart = document.getLineNumber(annotation.getStart());
        int lineStartOffset = document.getLineStartOffset(lineAtViolationStart);
        String lineText = document.getText(TextRange.create(lineStartOffset, document.getLineEndOffset(lineAtViolationStart)));

        //Calculate the indentation length by counting the whitespace characters at the beginning of the violation's line.
        var indentationLength = 0;
        while (Character.isWhitespace(lineText.charAt(indentationLength))) {
            indentationLength++;
        }

        //Insert a new line before the current one
        var startNewLineBeforeAction = (StartNewLineBeforeAction) action;
        startNewLineBeforeAction.actionPerformed(editor, EditorUtil.getEditorDataContext(editor));

        //Remove all whitespaces in the new line, so after adding the comment and indentation, users don't end up with extra whitespaces in the line
        int newlineNumber = document.getLineNumber(editor.getCaretModel().getOffset());
        int newLineStartOffset = document.getLineStartOffset(newlineNumber);
        document.deleteString(newLineStartOffset, document.getLineEndOffset(newlineNumber));

        //Get the comment sign for the current file
        var language = LanguageUtils.getLanguageFromFilename(psiFile.getName());
        var commentPrefix = io.codiga.plugins.jetbrains.utils.LanguageUtils.commentPrefixFor(language);

        //Insert the "codiga-disable" comment at the new line's start position
        document.insertString(newLineStartOffset, " ".repeat(indentationLength) + commentPrefix + " " + CODIGA_DISABLE);
    }
}
