package io.codiga.plugins.jetbrains.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Utility classes to send notification to IntelliJ.
 */
public final class NotificationUtils {

    public static final String NOTIFICATION_GROUP_API = "Codiga API";

    public static final String NOTIFICATION_NO_API_KEYS = "Codiga: please define your API keys in preferences";
    public static final String NOTIFICATION_API_KEYS_INCORRECT = "Codiga: incorrect API keys";

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Record the error already sent to projects
     */
    private static Set<Pair<String, String>> NOTIFICATION_ERROR_ONCE_PER_PROJECT = new ConcurrentSkipListSet<>();

    /**
     * Do not instantiate this class.
     */
    private NotificationUtils() {}

    /**
     * Notify an error for a project and do it only once.
     * @param project
     * @param message
     */
    public static void notififyProjectOnce(Project project, String message, String notificationGroupName) {
        final NotificationGroupManager notificationGroupManager = NotificationGroupManager.getInstance();
        final NotificationGroup notificationGroup = notificationGroupManager.getNotificationGroup(notificationGroupName);
        LOGGER.debug(notificationGroupManager.toString());
        LOGGER.debug(notificationGroup.toString());

        Pair<String, String> key = Pair.of(project.getName(), message);

        if(NOTIFICATION_ERROR_ONCE_PER_PROJECT.contains(key)) {
            LOGGER.debug(String.format("not showing notification %s again on project %s", message, project.getName()));
            return;
        } else {
            notificationGroup
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
            NOTIFICATION_ERROR_ONCE_PER_PROJECT.add(key);
        }

    }
}