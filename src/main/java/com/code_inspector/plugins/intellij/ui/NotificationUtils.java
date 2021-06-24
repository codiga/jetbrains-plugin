package com.code_inspector.plugins.intellij.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;

/**
 * Utility classes to send notification to IntelliJ.
 */
public final class NotificationUtils {

    public static final String NOTIFICATION_GROUP_API = "Code Inspector API";

    public static final String NOTIFICATION_NO_API_KEYS = "Code Inspector: please define your API keys in preferences";
    public static final String NOTIFICATION_API_KEYS_INCORRECT = "Code Inspector: incorrect API keys";

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Record the error already sent to projects.
     * The tuple is the name of the project and the message being sent.
     */
    private final static Set<Pair<String, String>> NOTIFICATION_ERROR_ONCE_PER_PROJECT = new ConcurrentSkipListSet<>();

    /**
     * Do not instantiate this class.
     */
    private NotificationUtils() {}

    /**
     * Notify an error for a project and do it only once.
     * @param project - the project to show a notification against
     * @param message - the message we want to show/highlight.
     */
    public static void notififyProjectOnce(Project project, String message, String notificationGroupName) {
        final NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        final NotificationGroup notificationGroup = notificationGroupManager.getNotificationGroup(notificationGroupName);
        LOGGER.debug(notificationGroupManager.toString());
        LOGGER.debug(notificationGroup.toString());

        Pair<String, String> key = Pair.of(project.getName(), message);

        if(NOTIFICATION_ERROR_ONCE_PER_PROJECT.contains(key)) {
            LOGGER.debug(String.format("not showing notification %s again on project %s", message, project.getName()));
        } else {
            notificationGroup
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
            NOTIFICATION_ERROR_ONCE_PER_PROJECT.add(key);
        }

    }
}
