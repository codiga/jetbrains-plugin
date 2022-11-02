package io.codiga.plugins.jetbrains.starter;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil.findCodigaConfigFile;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigState;
import io.codiga.plugins.jetbrains.rosie.CodigaRulesetConfigs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Initiates a notification popup to create the Codiga config file if there is a Python module in the project.
 * <p>
 * Initiates a background task the periodically updates the local Rosie rules cache.
 * <p>
 * This is separated from {@link AppStarter}, because if the YAML plugin is not installed or disabled,
 * the codiga.yml config file parsing cannot be performed.
 */
public class RosieStartupActivity implements StartupActivity {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Stores the Rosie cache updaters per project, so that they can be properly cancelled upon project close.
     * <p>
     * Limiting the initial capacity building on the assumption that users don't usually have a lot of projects
     * open at the same time.
     */
    private final Map<Project, ScheduledFuture<?>> rosieCacheUpdaters = new ConcurrentHashMap<>(4);

    private Notification notification;

    @Override
    public void runActivity(@NotNull Project project) {
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            startRosieRulesCacheUpdater(project);
        }
        showConfigureDefaultConfigFileNotification(project);
    }

    /**
     * Displays a notification popup, so that users are notified that they could have a Codiga config file configured with various rulesets.
     * <p>
     * The notification popup displays the following actions:
     * <ul>
     *     <li>action to create the config file with defualt Python rulesets</li>
     *     <li>action to never remind the user again for the current project. This state is stored in {@link CodigaConfigState}.</li>
     * </ul>
     * <p>
     * Currently, it looks only for Python modules in the project, other languages are not supported at the moment.
     * <p>
     * NOTE: if at project opening, there is a codiga.yml present, but the user deletes it, we notify them about the missing file,
     * the next time open the same project.
     */
    private void showConfigureDefaultConfigFileNotification(Project project) {
        var codigaConfig = CodigaConfigState.getInstance(project);

        //If the user hasn't decided to never be reminded about creating a config file
        if (codigaConfig.isShouldNotifyUserToCreateCodigaConfig()) {
            //If there is a config file in the project, we don't show the notification
            YAMLFile codigaConfigFile = findCodigaConfigFile(project);
            if (codigaConfigFile != null) {
                return;
            }

            Module[] modules = ModuleManager.getInstance(project).getModules();
            //If there is at least one Python module in the project, we are showing the notification
            // For an explanation on modules, see https://www.jetbrains.com/help/idea/creating-and-managing-modules.html
            boolean hasPythonModule = Arrays.stream(modules).map(ModuleType::get).map(ModuleType::getId).anyMatch("PYTHON_MODULE"::equals);

            if (hasPythonModule) {
                notification = NotificationGroupManager.getInstance().getNotificationGroup("Codiga API")
                    .createNotification("Check for security, code style in your Python code with Codiga", NotificationType.INFORMATION)
                    .addAction(new AnAction("Create a codiga.yml file to check code") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                            if (notification != null) {
                                notification.hideBalloon();
                                //At this point the project root dir must exist
                                var projectDir = ProjectUtil.guessProjectDir(project);
                                try {
                                    WriteAction.run(() -> {
                                        VirtualFile childData = projectDir.createChildData(this, CodigaConfigFileUtil.CODIGA_CONFIG_FILE_NAME);
                                        childData.setBinaryContent(CodigaRulesetConfigs.DEFAULT_PYTHON_RULESET_CONFIG.getBytes(StandardCharsets.UTF_8));
                                    });
                                } catch (IOException e) {
                                    LOGGER.error("Could not create codiga.yml in the project root directory.", e);
                                }
                            }
                        }
                    })
                    .addAction(new AnAction("Never remind me for this project") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                            if (notification != null) {
                                notification.hideBalloon();
                                codigaConfig.setShouldNotifyUserToCreateCodigaConfig(false);
                            }
                        }
                    });

                Notifications.Bus.notify(notification, project);
            }
        }
    }

    /**
     * Starts the updater for the {@link RosieRulesCache}.
     */
    private void startRosieRulesCacheUpdater(@NotNull Project project) {
        var updateHandler = new RosieRulesCacheUpdateHandler(RosieRulesCache.getInstance(project), project);
        var cacheUpdater = AppExecutorUtil.getAppScheduledExecutorService()
            .scheduleWithFixedDelay(updateHandler::handleCacheUpdate, 1L, 10L, SECONDS);

        rosieCacheUpdaters.put(project, cacheUpdater);

        //When the project has closed, stop the rules cache updater task
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(ProjectManager.TOPIC,
            new ProjectManagerListener() {
                @Override
                public void projectClosed(@NotNull Project project) {
                    /*
                     * Retrieving the updater from cache, as otherwise the subscription to this topic
                     * and cancelling the current updater is not bound to the current project.
                     * Simply calling 'cacheUpdater.cancel(true)' didn't seem to have any effect.
                     */
                    var updater = RosieStartupActivity.this.rosieCacheUpdaters.get(project);
                    if (updater != null) {
                        RosieStartupActivity.this.rosieCacheUpdaters.remove(project);
                        AppExecutorUtil.getAppScheduledExecutorService()
                            .schedule(() -> updater.cancel(true), 500, MILLISECONDS);
                        LOGGER.debug("[RosieRulesCacheUpdater] Cancelled Rosie rules cache updater for project: " + project.getName());
                    }
                }
            });
    }
}
