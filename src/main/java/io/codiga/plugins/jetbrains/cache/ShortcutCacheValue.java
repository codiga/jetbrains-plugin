package io.codiga.plugins.jetbrains.cache;

import io.codiga.api.GetRecipesForClientByShortcutQuery;

import java.util.List;

/**
 * The cache value contains the list of recipes but also
 * - the last timestamp on the server for all recipes for the associated key
 *   if the timestamp on the server did not change, we do not update
 *   the list of recipes
 * - last time we updated the list - we only update periodically and not every time
 *   we attempt to refresh the cache.
 */
public class ShortcutCacheValue {
    private final Long lastTimestampFromServer;
    private Long lastUpdateTimestamp;
    private final List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes;

    /**
     * Update at which we refresh the cache.
     */
    private final static Long UPDATE_PERIOD_MILLISECONDS = 10000L; // 10 seconds

    /**
     * We delete a value if not used for this period of time.
     */
    private final static Long DELETE_PERIOD_MILLISECONDS = 600000L; // 10 minutes

    public ShortcutCacheValue(List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes, long timestampServer) {
        this.recipes = recipes;
        this.lastTimestampFromServer = timestampServer;
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }

    public void updateUpdateTimestamp() {
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }


    /**
     * Explain if we should refresh/update the cache or not.
     * @return
     */
    public boolean needsUpdate() {
        Long currentTimestamp = System.currentTimeMillis();
        Long difference = currentTimestamp - this.lastUpdateTimestamp;
        return difference > UPDATE_PERIOD_MILLISECONDS;
    }

    /**
     * Return true if the value has not been used
     * @return
     */
    public boolean shouldBeDeleted() {
        Long currentTimestamp = System.currentTimeMillis();
        Long difference = currentTimestamp - this.lastUpdateTimestamp;
        return difference > DELETE_PERIOD_MILLISECONDS;
    }

    public long getLastTimestampFromServer() {
        return this.lastTimestampFromServer;
    }

    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipes() {
        return this.recipes;
    }
}
