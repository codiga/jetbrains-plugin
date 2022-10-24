package io.codiga.plugins.jetbrains.starter;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Initiates a background task the periodically updates the local Rosie rules cache.
 * <p>
 * This is separated from {@link AppStarter}, because if the YAML plugin is not installed or it is disabled,
 * the codiga.yml config file parsing cannot be performed.
 */
public class RosieRulesCacheUpdater implements StartupActivity {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Stores the Rosie cache updaters per project, so that they can be properly cancelled upon project close.
     * <p>
     * Limiting the initial capacity building on the assumption that users don't usually have a lot of projects
     * open at the same time.
     */
    private final Map<Project, ScheduledFuture<?>> rosieCacheUpdaters = new ConcurrentHashMap<>(4);

    @Override
    public void runActivity(@NotNull Project project) {
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            startRosieRulesCacheUpdater(project);
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
                    var updater = RosieRulesCacheUpdater.this.rosieCacheUpdaters.get(project);
                    if (updater != null) {
                        RosieRulesCacheUpdater.this.rosieCacheUpdaters.remove(project);
                        AppExecutorUtil.getAppScheduledExecutorService()
                            .schedule(() -> updater.cancel(true), 500, MILLISECONDS);
                        LOGGER.debug("[RosieRulesCacheUpdater] Cancelled Rosie rules cache updater for project: " + project.getName());
                    }
                }
            });
    }
}
