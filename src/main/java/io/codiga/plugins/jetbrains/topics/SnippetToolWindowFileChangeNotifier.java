package io.codiga.plugins.jetbrains.topics;

import com.intellij.util.messages.Topic;

/**
 * This topic represent when there was a file change on the file system.
 * In that case, we refresh the snippets shown in the snippet search.
 */
public interface SnippetToolWindowFileChangeNotifier {

    Topic<SnippetToolWindowFileChangeNotifier> CODIGA_NEW_FILE_SELECTED_TOPIC =
        Topic.create("New file selected", SnippetToolWindowFileChangeNotifier.class);

    void beforeAction(Object context);

    void afterAction(Object context);
}
