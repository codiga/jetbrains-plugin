package io.codiga.plugins.jetbrains.topics;

import com.intellij.util.messages.Topic;

/**
 * Topic that represent what a change of the API key.
 * <p>
 * We subscribe to this topic in the preferences when the API key change in order to refresh the list
 * of projects.
 */
public interface CodigaEnabledStatusNotifier extends ChangeNotifier {

    Topic<CodigaEnabledStatusNotifier> CODIGA_ENABLED_CHANGE_TOPIC =
        Topic.create("Codiga inline completion change", CodigaEnabledStatusNotifier.class);
}
