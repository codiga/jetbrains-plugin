package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class SnippetToolWindowFileEditorManagerListener implements FileEditorManagerListener {
    private static Project currentProject = null;
    private static VirtualFile currentVirtualFile = null;
    private static FileEditor currentFileEditor = null;
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public static Project getCurrentProject() {
        return currentProject;
    }

    public static VirtualFile getCurrentVirtualFile() {
        return currentVirtualFile;
    }

    public static FileEditor getCurrentFileEditor() {
        return currentFileEditor;
    }


    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditor fileEditor = event.getNewEditor();
        if (fileEditor == null || fileEditor.getFile() == null) {
            return;
        }


        SnippetToolWindow snippetToolWindow = SnippetToolWindowFactory.getSnippetToolWindow();
        if (snippetToolWindow == null) {
            return;
        }

        snippetToolWindow.setLoading(true);
        currentProject = event.getManager().getProject();
        currentVirtualFile = fileEditor.getFile();
        currentFileEditor = fileEditor;

        ApplicationManager.getApplication().executeOnPooledThread(() -> snippetToolWindow.updateEditor(event.getManager().getProject(),  fileEditor.getFile(), Optional.empty(), true));
    }
}
