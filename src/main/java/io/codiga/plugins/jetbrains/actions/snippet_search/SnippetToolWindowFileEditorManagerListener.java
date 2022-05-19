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

/**
 * This class listens when the editor changes and updates the snippets available
 * in the window toolbar.
 */
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


    /**
     * This function is triggered every time the user changes the editor.
     * @param event
     */
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditor fileEditor = event.getNewEditor();
        SnippetToolWindow snippetToolWindow = SnippetToolWindowFactory.getSnippetToolWindow();

        // Snippet tool not created or initialize: do not do anything
        if (snippetToolWindow == null) {
            return;
        }

        // there is no new file selected.
        if (fileEditor == null || fileEditor.getFile() == null) {
            snippetToolWindow.updateNoEditor();
            return;
        }

        snippetToolWindow.setLoading(true);
        currentProject = event.getManager().getProject();
        currentVirtualFile = fileEditor.getFile();
        currentFileEditor = fileEditor;

        /**
         * Execute on pool thread so that we do not block the main thread
         * and do not cause IntelliJ to hang forever.
         */
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            snippetToolWindow.updateEditor(
                    event.getManager().getProject(),
                    fileEditor.getFile(),
                    Optional.empty(),
                    true);
        });
    }
}
