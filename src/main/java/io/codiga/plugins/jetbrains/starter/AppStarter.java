package io.codiga.plugins.jetbrains.starter;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsConfigurable;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Start this code after the project is initialized.
 * Check that the API keys are configured and if they are not configured,
 * ask the user to configure them in the preferences.
 */
public class AppStarter implements StartupActivity {
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final AppSettingsState appSettingsState = AppSettingsState.getInstance();
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private Notification notification;

    @Override
    public void runActivity(@NotNull Project project) {
        /**
         * Check if we can get the user via the GraphQL API.
         * If that does not work, show a balloon asking to enter the API keys.
         */
        if (codigaApi.getUsername().isPresent()) {
            return;
        }
        notification = NotificationGroupManager.getInstance().getNotificationGroup("Codiga API")
                .createNotification("Configure your API keys to get access to your recipes from Codiga.", NotificationType.INFORMATION)
                .setSubtitle("Codiga API keys not set or incorrect")
                .addAction(new AnAction("Set API keys") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        if (notification != null) {
                            notification.hideBalloon();
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, AppSettingsConfigurable.class);
                        }

                    }
                })
                .addAction(new AnAction("Hide") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        if (notification != null) {
                            notification.hideBalloon();
                        }

                    }
                })
                .addAction(new AnAction("Never show again") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        appSettingsState.setShowDialogApiNotification(false);
                        if (notification != null) {
                            notification.hideBalloon();
                        }

                    }
                });
        if (appSettingsState.getShowDialogApiNotification()) {
            Notifications.Bus.notify(notification, project);
        }
    }
}
