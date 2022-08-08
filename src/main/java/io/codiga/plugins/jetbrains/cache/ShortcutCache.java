package io.codiga.plugins.jetbrains.cache;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * This is the shortcut cache that contains all the recipes that have
 * a shortcut. It stores all the recipes for all the files being
 * opened in the editor.
 */
public final class ShortcutCache {
    // Uses a concurrent hashmap to avoid threading issues.
    ConcurrentHashMap<ShortcutCacheKey, ShortcutCacheValue> cache;

    /**
     * Last activity of the user on the system.
     */
    private long lastActivityTimestamp;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    private static ShortcutCache _INSTANCE = new ShortcutCache();

    private static long TEN_MINUTES_IN_MILLISECONDS = 10 * 60 * 1000;


    private ShortcutCache() {
        cache = new ConcurrentHashMap<>();
        lastActivityTimestamp = System.currentTimeMillis();
    }

    public static ShortcutCache getInstance() {
        return _INSTANCE;
    }

    public void updateLastActivityTimestamp() {
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    /**
     * Return true if the user was active in the last ten minutes.
     * @return
     */
    public boolean wasActiveRecently() {
        long tenMinutesAgo = System.currentTimeMillis() - TEN_MINUTES_IN_MILLISECONDS;
        return lastActivityTimestamp > tenMinutesAgo;
    }



    /**
     * Update values for a key by pulling the API.
     *  - check the time on the server for the latest update
     *  - if there is already a cache value,
     *    - update only if the timestamp from the server is different
     *    - if timestamp did not change, update the access time.
     *  - if there is no value in the cache, fetch
     *
     * @param shortcutCacheKey
     */
    private void updateKey(ShortcutCacheKey shortcutCacheKey) {
        LOGGER.info("refreshing cache key " + shortcutCacheKey.toString());
        Optional<Long> lastUpdateTimestamp = codigaApi.getRecipesForClientByShotcurtLastTimestmap(shortcutCacheKey.getDependencies(), shortcutCacheKey.getLanguage());
        boolean shouldFetch = false;
        if (!lastUpdateTimestamp.isPresent()) {
            return;
        }

        if (cache.containsKey(shortcutCacheKey)) {
            ShortcutCacheValue shortcutCacheValue = cache.get(shortcutCacheKey);
            if(shortcutCacheValue.getLastTimestampFromServer() != lastUpdateTimestamp.get()) {
                shouldFetch = true;
            }
        } else {
            shouldFetch = true;
        }


        if (shouldFetch) {
            List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes =
                    codigaApi.getRecipesForClientByShotcurt(Optional.empty(),
                            shortcutCacheKey.getDependencies(),
                            Optional.empty(),
                            shortcutCacheKey.getLanguage(),
                            shortcutCacheKey.getFilename(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty());
            ShortcutCacheValue shortcutCacheValue = new ShortcutCacheValue(recipes, lastUpdateTimestamp.get());
            cache.put(shortcutCacheKey, shortcutCacheValue);
        } else {
            ShortcutCacheValue shortcutCacheValue = cache.get(shortcutCacheKey);
            shortcutCacheValue.updateUpdateTimestamp();
            cache.put(shortcutCacheKey, shortcutCacheValue);
        }
    }

    /**
     * Refresh a cache key in the cache if and only if it needs to be updated.
     * Only update if there was no activity for 10 minutes.
     * @param shortcutCacheKey
     */
    public void refreshCacheKey(final ShortcutCacheKey shortcutCacheKey) {
        try {
            if(!wasActiveRecently()) {
                LOGGER.debug("was not active recently, do not refresh");
                return;
            }

            if (cache.containsKey(shortcutCacheKey)) {
                ShortcutCacheValue shortcutCacheValue = cache.get(shortcutCacheKey);
                if (shortcutCacheValue.needsUpdate()) {
                    LOGGER.debug("Not updating the following key, do not need refresh: " + shortcutCacheKey.toString());
                    updateKey(shortcutCacheKey);
                }
            } else {
                updateKey(shortcutCacheKey);
            }

        } catch (NullPointerException npe) {
            LOGGER.info("error when refreshing the cache");
        }
    }

    /**
     * Just query the cache and gets the value that it contains.
     * This method never queries the API.
     * @param shortcutCacheKey - the key we are querying
     * @return - the list of recipes if any
     */
    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipesShortcut(ShortcutCacheKey shortcutCacheKey) {
        try{
            if (cache.containsKey(shortcutCacheKey)) {
                return cache.get(shortcutCacheKey).getRecipes();
            } else {
                return ImmutableList.of();
            }
        } catch (NullPointerException npe) {
            return ImmutableList.of();
        }
    }

    /**
     * Garbage collect the cache and remove keys that have not been
     * used for a long time.
     */
    public void garbageCollect() {
        List<ShortcutCacheKey> keysToRemove = new ArrayList<>();

        // Get the keys to remove
        for (ShortcutCacheKey key: cache.keySet()){
            ShortcutCacheValue value = cache.get(key);
            if (value.shouldBeDeleted()) {
                keysToRemove.add(key);
            }
        }

        // Remove from the mp
        for(ShortcutCacheKey key: keysToRemove) {
            cache.remove(key);
        }
    }

}
