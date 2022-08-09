package io.codiga.plugins.jetbrains.utils;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class EditorUtils {

    private EditorUtils () {
        // no code, private util class
    }

    @Nullable
    public static Editor getActiveEditor(@NotNull Document document) {
        if (!ApplicationManager.getApplication().isDispatchThread()) {
            return null;
        }

        Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        // ignore caret placing when exiting
        Editor activeEditor =
            ApplicationManager.getApplication().isDisposed()
                ? null
                : CommonDataKeys.EDITOR.getData(dataContext);

        if (activeEditor != null && activeEditor.getDocument() != document) {
            activeEditor = null;
        }

        return activeEditor;
    }
}
