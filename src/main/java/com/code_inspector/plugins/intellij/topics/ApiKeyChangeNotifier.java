package com.code_inspector.plugins.intellij.topics;

import com.intellij.util.messages.Topic;

/**
 * Topic that represent what a change of the API key.
 *
 * We subscribe to this topic in the preferences when the API key change in order to refresh the list
 * of projects.
 */
public interface ApiKeyChangeNotifier {
    Topic<ApiKeyChangeNotifier> CODE_INSPECTOR_API_KEY_CHANGE_TOPIC =
        Topic.create("Code Inspector API key change", ApiKeyChangeNotifier.class);

    void beforeAction(Object context);
    void afterAction(Object context);
}
