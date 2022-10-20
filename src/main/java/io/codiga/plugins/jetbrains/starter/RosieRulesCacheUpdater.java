package io.codiga.plugins.jetbrains.starter;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.services.CodigaConfigFileUtil.collectRulesetNames;
import static io.codiga.plugins.jetbrains.services.CodigaConfigFileUtil.findCodigaConfigFile;
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
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

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
    private final CodigaApi codigaApi = CodigaApi.getInstance();

    /**
     * Stores the Rosie cache updaters per project, so that they can be properly cancelled upon project close.
     * <p>
     * Limiting the initial capacity building on the assumption that users don't usually have a lot of projects
     * open at the same time.
     */
    private final Map<Project, ScheduledFuture<?>> rosieCacheUpdaters = new ConcurrentHashMap<>(4);

    @Override
    public void runActivity(@NotNull Project project) {
        startRosieRulesCacheUpdater(project);
    }

    /**
     * Starts the updater for the {@link RosieRulesCache}.
     */
    private void startRosieRulesCacheUpdater(@NotNull Project project) {
        var rulesCache = RosieRulesCache.getInstance(project);
        var cacheUpdater = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            if (project.isDisposed()) {
                return;
            }

            YAMLFile codigaConfigFile = findCodigaConfigFile(project);
            if (codigaConfigFile == null) {
                rulesCache.clear();
                return;
            }

            //There was a change in the codiga.yml file
            if (rulesCache.hasDifferentModificationStampThan(codigaConfigFile)) {
                rulesCache.saveModificationStampOf(codigaConfigFile);
                var rulesetNames = collectRulesetNames(codigaConfigFile);

                //Since there was a config change locally, and there is at least one ruleset name configured,
                // query to the Codiga server must be sent
                if (!rulesetNames.isEmpty()) {
                    codigaApi.getRulesetsForClient(rulesetNames).ifPresent(rulesets -> {
                        rulesCache.updateCacheFrom(rulesets);

                        /*
                          Updating the local timestamp only if it has changed, because it may happen that
                          codiga.yml was updated locally with a non-existent ruleset, or a ruleset that has an earlier timestamp,
                          than the latest updated one, so the rulesets configured don't result in an updated timestamp from the server.
                         */
                        codigaApi.getRulesetsLastTimestamp(rulesetNames)
                            .filter(timestamp -> timestamp != rulesCache.getLastUpdatedTimeStamp())
                            .ifPresent(rulesCache::setLastUpdatedTimeStamp);
                    });
                } else {
                    rulesCache.clear();
                }
            }
            //The codiga.yml file is unchanged
            else {
                var rulesetNames = collectRulesetNames(codigaConfigFile);
                if (!rulesetNames.isEmpty()) {
                    /*
                      If any of the rulesets have changed on the Codiga server, compared to what we have in the local cache,
                      update the cache.
                      If only non-existent ruleset names are sent, Optional.empty() is returned, thus no cache update happens.
                     */
                    codigaApi.getRulesetsLastTimestamp(rulesetNames)
                        .filter(timestamp -> timestamp != rulesCache.getLastUpdatedTimeStamp())
                        .ifPresent(timestamp ->
                            codigaApi.getRulesetsForClient(rulesetNames).ifPresent(rulesets -> {
                                rulesCache.updateCacheFrom(rulesets);
                                rulesCache.setLastUpdatedTimeStamp(timestamp);
                                LOGGER.debug("[RosieRulesCacheUpdater] Updated rulesets and timestamp in local Rosie cache for project: " + project.getName());
                            }));
                }
            }
        }, 1L, 10L, SECONDS);

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
