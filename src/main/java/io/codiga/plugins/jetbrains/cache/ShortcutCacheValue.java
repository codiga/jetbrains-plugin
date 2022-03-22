package io.codiga.plugins.jetbrains.cache;

import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;

import java.util.List;
import java.util.Objects;


public class ShortcutCacheValue {
    private Long lastTimestampFromServer;
    private Long lastUpdateTimestamp;
    private List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes;

    /**
     * Update at which we refresh the cache.
     */
    private Long UPDATE_PERIOD_MILLISECONDS = 10000L;

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

    public long getLastTimestampFromServer() {
        return this.lastTimestampFromServer;
    }

    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipes() {
        return this.recipes;
    }
}
