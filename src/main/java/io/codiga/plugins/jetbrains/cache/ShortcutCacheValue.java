package io.codiga.plugins.jetbrains.cache;

import io.codiga.api.GetRecipesForClientByShortcutQuery;

import java.util.List;


public class ShortcutCacheValue {
    private Long lastTimestampFromServer;
    private Long lastUpdateTimestamp;
    private List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes;

    /**
     * Update at which we refresh the cache.
     */
    private Long UPDATE_PERIOD_MILLISECONDS = 10000L; // 10 seconds

    /**
     * We delete a value if not used for this period of time.
     */
    private Long DELETE_PERIOD_MILLISECONDS = 600000L; // 10 minutes

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
        if (difference > UPDATE_PERIOD_MILLISECONDS) {
            return true;
        }
        return false;
    }

    /**
     * Return true if the value has not been used
     * @return
     */
    public boolean shouldBeDeleted() {
        Long currentTimestamp = System.currentTimeMillis();
        Long difference = currentTimestamp - this.lastUpdateTimestamp;
        if (difference > DELETE_PERIOD_MILLISECONDS) {
            return true;
        }
        return false;
    }

    public long getLastTimestampFromServer() {
        return this.lastTimestampFromServer;
    }

    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipes() {
        return this.recipes;
    }
}
