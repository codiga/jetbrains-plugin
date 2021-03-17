package com.code_inspector.plugins.intellij.cache;

import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;

/**
 * This class implements a global cache for the whole plugin to avoid fetching
 * data from the API at each refresh. We are using a ConcurrentHashMap because
 * multiple threads may attempt to use this cache.
 *
 * This cache fetches the data if the data is not in the cache already.
 * It does so using the CodeInspectorApi service.
 */
public final class AnalysisDataCache {
    ConcurrentHashMap<CacheKey, Optional<GetFileDataQuery.Project>> cache;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodeInspectorApi codeInspectorApi = ServiceManager.getService(CodeInspectorApi.class);
    private static AnalysisDataCache _INSTANCE = new AnalysisDataCache();

    private AnalysisDataCache() {
        cache = new ConcurrentHashMap<>();
    }

    public static AnalysisDataCache getInstance() {
        return _INSTANCE;
    }

    public Optional<GetFileDataQuery.Project> getData(Long projectId, String revision, String path) {
        CacheKey cacheKey = new CacheKey(projectId, revision, path);
        if (!cache.containsKey(cacheKey)) {
            LOGGER.debug(String.format("[AnalysisDataCache] cache miss, fetching from API for key %s", cacheKey));
            Optional<GetFileDataQuery.Project> query = codeInspectorApi.getDataForFile(projectId, revision, path);
            cache.put(cacheKey, query);
        }

        return cache.get(cacheKey);
    }

    public void invalidateCache() {
        LOGGER.debug("invalidating cache");
        this.cache.clear();
    }
}
