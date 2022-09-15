package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import io.codiga.plugins.jetbrains.topics.SnippetToolWindowFileChangeNotifier;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.topics.SnippetToolWindowFileChangeNotifier.CODIGA_NEW_FILE_SELECTED_TOPIC;

/**
 * This class listens when the editor changes and updates the snippets available
 * in the window toolbar.
 */
public class SnippetToolWindowFileEditorManagerListener implements FileEditorManagerListener {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    final SnippetToolWindowFileChangeNotifier newFileTopic =
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CODIGA_NEW_FILE_SELECTED_TOPIC);

    /**
     * This function is triggered every time the user changes the editor.
     * @param event
     */
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        /**
         * Send a message with MessageBus to the snippet search that
         * we want to refresh the content.
         */
        newFileTopic.afterAction(null);

    }
}
