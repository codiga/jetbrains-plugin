package io.codiga.plugins.jetbrains.cache;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * This class just refresh the last actiivty timestamp from the cache so that we
 * do not refresh the cache when the editor is not active.
 */
public class TypedKeyHandler extends TypedHandlerDelegate {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    @Override
    public Result charTyped(char c, Project project, Editor editor, PsiFile file) {
        ShortcutCache.getInstance().updateLastActivityTimestamp();
        return Result.CONTINUE;
    }
}
