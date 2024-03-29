package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Clear the suggestions when the user types escape.
 */
public class EscapeHandler extends EditorActionHandler {

    @Override
    public void doExecute(@NotNull Editor editor, Caret caret, DataContext dataContext) {
        SnippetPreview snippetPreview = SnippetPreview.getInstance(editor);
        if (snippetPreview != null){
            SnippetPreview.clear(editor);
        }
    }

    @Override
    public boolean isEnabledForCaret(
        @NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
        SnippetPreview snippetPreview = SnippetPreview.getInstance(editor);
        return snippetPreview != null;
    }
}
