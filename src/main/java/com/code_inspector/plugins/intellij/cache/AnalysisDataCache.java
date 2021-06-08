package com.code_inspector.plugins.intellij.cache;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;

/**
 * This class implements a global cache for the whole plugin to avoid fetching
 * data from the API at each refresh. We are using a ConcurrentHashMap because
 * multiple threads may attempt to use this cache.
 *
 * This cache fetches the data if the data is not in the cache already.
 * It does so using the CodeInspectorApi service.
 */
public final class AnalysisDataCache {
    ConcurrentHashMap<CacheKey, Optional<GetFileDataQuery.Project>> cacheProjectAnalysis;
    ConcurrentHashMap<CacheKey, Optional<GetFileAnalysisQuery.GetFileAnalysis>> cacheFileAnalysis;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodeInspectorApi codeInspectorApi = ServiceManager.getService(CodeInspectorApi.class);
    private static AnalysisDataCache _INSTANCE = new AnalysisDataCache();

    private AnalysisDataCache() {
        cacheProjectAnalysis = new ConcurrentHashMap<>();
        cacheFileAnalysis = new ConcurrentHashMap<>();
    }

    public static AnalysisDataCache getInstance() {
        return _INSTANCE;
    }

    private String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] raw = md.digest(input.getBytes());
            return DatatypeConverter.printHexBinary(raw);
        } catch (NoSuchAlgorithmException e){
            return null;
        }
    }

    public Optional<GetFileAnalysisQuery.GetFileAnalysis> getViolationsFromFileAnalysis(Long projectId, String filename, String code) {
        String digest = getMD5(code);;
        CacheKey cacheKey = new CacheKey(projectId, null, filename, digest);
        Optional<Long> projectIdOptional = Optional.ofNullable(projectId);
        LanguageEnumeration language = getLanguageFromFilename(filename);

        if (language == LanguageEnumeration.UNKNOWN) {
            LOGGER.debug(String.format("Language unknown for file %s", filename));
            return Optional.empty();
        }

        LOGGER.debug(String.format("Language file %s: %s", filename, language));

        if (!cacheProjectAnalysis.containsKey(cacheKey)) {
            LOGGER.debug(String.format("[AnalysisDataCache] cache miss, fetching from API for key %s", cacheKey));
            Optional<GetFileAnalysisQuery.GetFileAnalysis> query = codeInspectorApi.getFileAnalysis(filename, code, language, projectIdOptional);
            cacheFileAnalysis.put(cacheKey, query);
        } else {
            LOGGER.debug(String.format("[AnalysisDataCache] cache hit on key %s", cacheKey));
        }

        return cacheFileAnalysis.get(cacheKey);
    }


    public Optional<GetFileDataQuery.Project> getViolationsFromProjectAnalysis(Long projectId, String revision, String path) {
        CacheKey cacheKey = new CacheKey(projectId, revision, path, null);
        if (!cacheProjectAnalysis.containsKey(cacheKey)) {
            LOGGER.debug(String.format("[AnalysisDataCache] cache miss, fetching from API for key %s", cacheKey));
            Optional<GetFileDataQuery.Project> query = codeInspectorApi.getDataForFile(projectId, revision, path);
            cacheProjectAnalysis.put(cacheKey, query);
        }

        return cacheProjectAnalysis.get(cacheKey);
    }

    public void invalidateCache() {
        LOGGER.debug("invalidating cache");
        this.cacheProjectAnalysis.clear();
    }
}
