package io.codiga.plugins.jetbrains.starter;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil.collectRulesetNames;
import static io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil.findCodigaConfigFile;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import lombok.RequiredArgsConstructor;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * Handles updating the {@link RosieRulesCache}. This is executed periodically via {@link RosieStartupActivity}.
 */
@RequiredArgsConstructor
public final class RosieRulesCacheUpdateHandler {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodigaApi codigaApi = CodigaApi.getInstance();
    private final RosieRulesCache rulesCache;
    private final Project project;

    /**
     * First initialization of the ruleset names in cache.
     * <p>
     * Further ruleset name caching happens only when the Codiga config file is modified.
     */
    public void initRulesets() {
        YAMLFile codigaConfigFile = findCodigaConfigFile(project);
        if (isCodigaConfigFileExist(codigaConfigFile)) {
            rulesCache.setRulesetNames(collectRulesetNames(codigaConfigFile));
            rulesCache.saveModificationStampOf(codigaConfigFile);
        }
    }

    public void handleCacheUpdate() {
        if (project.isDisposed()) {
            return;
        }

        YAMLFile codigaConfigFile = findCodigaConfigFile(project);
        if (!isCodigaConfigFileExist(codigaConfigFile)) {
            rulesCache.clear();
            //Since the config file no longer exist, its modification stamp is reset too
            rulesCache.setConfigFileModificationStamp(0);
            return;
        }

        if (rulesCache.hasDifferentModificationStampThan(codigaConfigFile)) {
            updateCacheFromModifiedCodigaConfigFile(codigaConfigFile);
        } else {
            updateCacheFromChangesOnServer();
        }
    }

    private boolean isCodigaConfigFileExist(YAMLFile codigaConfigFile) {
        return codigaConfigFile != null
            //While the PsiFile may still exist, the underlying VirtualFile may not be valid, or exist at all
            && codigaConfigFile.getVirtualFile().exists()
            && codigaConfigFile.getVirtualFile().isValid();
    }

    //There was a change in the codiga.yml file
    private void updateCacheFromModifiedCodigaConfigFile(YAMLFile codigaConfigFile) {
        rulesCache.saveModificationStampOf(codigaConfigFile);
        var rulesetNames = collectRulesetNames(codigaConfigFile);
        rulesCache.setRulesetNames(rulesetNames);

        //Since there was a config change locally, and there is at least one ruleset name configured,
        // query to the Codiga server must be sent.
        if (!rulesetNames.isEmpty()) {
            codigaApi.getRulesetsForClient(rulesetNames).ifPresent(rulesets -> {
                /*
                  If the server returns no rulesets, e.g. due to misconfiguration of codiga.yml,
                  we clear the cache. NOTE: this doesn't take into account if no ruleset is returned
                  due to an issue in how the Codiga server collects the rules.
                 */
                if (rulesets.isEmpty()) {
                    rulesCache.clear();
                    return;
                }

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
    private void updateCacheFromChangesOnServer() {
        var rulesetNames = rulesCache.getRulesetNames();
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
                        LOGGER.debug("[RosieRulesCacheUpdateHandler] Updated rulesets and timestamp in local Rosie cache for project: " + project.getName());
                    }));
        }
    }
}
