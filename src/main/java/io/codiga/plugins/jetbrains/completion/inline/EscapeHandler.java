package io.codiga.plugins.jetbrains.completion.inline;
import com.intellij.openapi.diagnostic.Logger;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class EscapeHandler extends EditorActionHandler {

    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);


    @Override
    public void doExecute(@NotNull Editor editor, Caret caret, DataContext dataContext) {
        LOGGER.info("execute escape handler");
        SnippetPreview.clear(editor);
    }

    @Override
    public boolean isEnabledForCaret(
        @NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
        LOGGER.info("execute isEnabledForCaret");
        return true;
    }
}
