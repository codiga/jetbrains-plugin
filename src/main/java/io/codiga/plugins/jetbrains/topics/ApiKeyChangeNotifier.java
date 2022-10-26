package io.codiga.plugins.jetbrains.topics;

import com.intellij.util.messages.Topic;

/**
 * Topic that represent what a change of the API key.
 *
 * We subscribe to this topic in the preferences when the API key change in order to refresh the list
 * of projects.
 */
public interface ApiKeyChangeNotifier extends ChangeNotifier{
    Topic<ApiKeyChangeNotifier> CODIGA_API_KEY_CHANGE_TOPIC =
        Topic.create("Codiga API key change", ApiKeyChangeNotifier.class);
}
