package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the Codiga snippets tool window. This is the main entrypoint
 * to create the tool window, the business logic is in SnippetToolWindow
 * itself.
 */
public class SnippetToolWindowFactory implements ToolWindowFactory {
    private static SnippetToolWindow snippetToolWindow = null;


    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Make sure the current project is set.
        SnippetToolWindowFileEditorManagerListener.setCurrentProject(project);
        VirtualFile[] virtualFiles = FileEditorManager.getInstance(project).getSelectedFiles();

        // Make sure we have one virtual file present.
        if (virtualFiles.length > 0) {
            SnippetToolWindowFileEditorManagerListener.setCurrentVirtualFile(virtualFiles[0]);
        }

        snippetToolWindow = new SnippetToolWindow(toolWindow, project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(snippetToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static SnippetToolWindow getSnippetToolWindow() {
        return snippetToolWindow;
    }
}
