package io.codiga.plugins.jetbrains.topics;

/**
 * Common interface for classes that provide {@link com.intellij.util.messages.Topic}s for subscribing to various changes.
 */
public interface ChangeNotifier {

    /**
     * No-op by default.
     */
    default void beforeAction(Object context) {
    }

    void afterAction(Object context);
}
