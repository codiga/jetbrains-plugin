package io.codiga.plugins.jetbrains.topics;

import com.intellij.util.messages.Topic;

/**
 * Represent if Codiga is active at all.
 * This topic is synced between the preferences and the status bar icon.
 */
public interface CodigaEnabledStatusNotifier {

    Topic<CodigaEnabledStatusNotifier> CODIGA_ENABLED_CHANGE_TOPIC =
        Topic.create("Codiga inline completion change", CodigaEnabledStatusNotifier.class);

    void beforeAction(Object context);

    void afterAction(Object context);
}
