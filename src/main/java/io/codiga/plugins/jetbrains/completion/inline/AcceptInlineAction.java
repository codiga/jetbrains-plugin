package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Action to accept the snippet and insert it
 * in the IDE.
 */
public class AcceptInlineAction extends EditorAction {

    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    protected AcceptInlineAction() {
        super(new AcceptInlineCompletionEditorAction());
    }

    private static class AcceptInlineCompletionEditorAction extends EditorWriteActionHandler {

        /**
         * Ensure that we currently have a preview available for this editor
         * (which means we have an instance of SnippetPreview ready)
         * @param editor
         * @param caret
         * @param dataContext
         * @return
         */
        @Override
        public boolean isEnabledForCaret(@NotNull Editor editor, Caret caret, DataContext dataContext) {
            return SnippetPreview.getInstance(editor) != null;
        }

        /**
         * Apply the snippet once the action is triggered.
         * @param editor
         * @param caret
         * @param dataContext
         */
        @Override
        public void executeWriteAction(Editor editor, Caret caret, DataContext dataContext) {
            SnippetPreview snippetPreview = SnippetPreview.getInstance(editor);
            if (snippetPreview != null){
                snippetPreview.addSnippetToEditor(caret);
            }
        }
    }
}
