package io.codiga.plugins.jetbrains.starter;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.cache.ShortcutCache;
import io.codiga.plugins.jetbrains.cache.ShortcutCacheKey;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsConfigurable;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getLanguageFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.graphql.Constants.CODING_ASSISTANT_DOCUMENTATION_URL;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Start this code after the project is initialized.
 * Check that the API keys are configured and if they are not configured,
 * ask the user to configure them in the preferences.
 */
public class AppStarter implements StartupActivity {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final AppSettingsState appSettingsState = AppSettingsState.getInstance();
    private Notification notification;

    @Override
    public void runActivity(@NotNull Project project) {
        /**
         * Check if we can get the user via the GraphQL API.
         * If that does not work, show a balloon asking to enter the API keys.
         */
        if (!codigaApi.getUsername().isPresent()) {
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

        /**
         * Make sure one editor is being opened at the file. Otherwise, the Coding Assistant cannot get the context
         * and will crash.
         */
        if (appSettingsState.getShowDialogOnboarding() &&
            FileEditorManager.getInstance(project) != null &&
            FileEditorManager.getInstance(project).getSelectedEditor() != null) {
            notification = NotificationGroupManager.getInstance().getNotificationGroup("Codiga API")
                .createNotification("Just type . or press CMD + ALT + S for shortcuts, CMD + ALT + C for searching snippets", NotificationType.INFORMATION)
                .setTitle("Codiga Coding Assistant")
                .addAction(new AnAction("Snippets") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        ActionManager.getInstance().getAction("com.code_inspector.plugins.intellij.actions.AssistantUseRecipeAction")
                            .actionPerformed(new AnActionEvent(null,
                                DataManager.getInstance().getDataContext(FileEditorManager.getInstance(project).getSelectedEditor().getComponent()),
                                ActionPlaces.UNKNOWN,
                                new Presentation(),
                                ActionManager.getInstance(), 0));

                    }
                })
                .addAction(new AnAction("Shortcuts") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        ActionManager.getInstance().getAction("com.code_inspector.plugins.intellij.actions.AssistantListShortcuts")
                            .actionPerformed(new AnActionEvent(null,
                                DataManager.getInstance().getDataContext(FileEditorManager.getInstance(project).getSelectedEditor().getComponent()),
                                ActionPlaces.UNKNOWN,
                                new Presentation(),
                                ActionManager.getInstance(), 0));

                    }
                })
                .addAction(new AnAction("Doc") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        try {
                            Desktop.getDesktop().browse(new URI(CODING_ASSISTANT_DOCUMENTATION_URL));
                        } catch (IOException | URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }
                })
                .addAction(new AnAction("Hide Forever") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        appSettingsState.setShowDialogOnboarding(false);
                        if (notification != null) {
                            notification.hideBalloon();
                        }

                    }
                });
            Notifications.Bus.notify(notification, project);
        }

        /**
         * Start the cache refresher.
         */
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                final AppSettingsState settings = AppSettingsState.getInstance();
                if (settings == null) {
                    return;
                }

                if (!settings.getCodigaEnabled()) {
                    LOGGER.debug("Codiga disabled, not refreshing cache");
                    return;
                }

                if (!settings.getUseInlineCompletion()) {
                    LOGGER.debug("Completion are disabled, not refreshing cache");
                    return;
                }
                
                for (FileEditor fileEditor : FileEditorManager.getInstance(project).getAllEditors()) {
                    if (fileEditor.getFile() == null) {
                        continue;
                    }
                    String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, fileEditor.getFile());
                    java.util.List<String> dependencies = DependencyManagement.getInstance().getDependencies(project, fileEditor.getFile()).stream().map(v -> v.getName()).collect(Collectors.toList());
                    LanguageEnumeration languageEnumeration = getLanguageFromEditorForVirtualFile(fileEditor.getFile());
                    ShortcutCacheKey shortcutCacheKey = new ShortcutCacheKey(languageEnumeration, filename, dependencies);
                    ShortcutCache.getInstance().refreshCacheKey(shortcutCacheKey);
                }

                ShortcutCache.getInstance().garbageCollect();

            }
        }, 1, 10L, SECONDS);
    }
}
