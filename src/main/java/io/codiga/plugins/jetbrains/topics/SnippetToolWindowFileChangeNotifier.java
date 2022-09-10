package io.codiga.plugins.jetbrains.topics;

import com.intellij.util.messages.Topic;

/**
 * Topic that represent what a change of the API key.
 *
 * We subscribe to this topic in the preferences when the API key change in order to refresh the list
 * of projects.
 */
public interface SnippetToolWindowFileChangeNotifier {

    Topic<SnippetToolWindowFileChangeNotifier> CODIGA_NEW_FILE_SELECTED_TOPIC =
        Topic.create("New file selected", SnippetToolWindowFileChangeNotifier.class);

    void beforeAction(Object context);
    void afterAction(Object context);
}
