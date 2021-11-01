package com.code_inspector.plugins.intellij.cache;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.graphql.GraphQlQueryException;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;

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
    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);
    private static AnalysisDataCache _INSTANCE = new AnalysisDataCache();

    private AnalysisDataCache() {
        cacheProjectAnalysis = new ConcurrentHashMap<>();
        cacheFileAnalysis = new ConcurrentHashMap<>();
    }

    public static AnalysisDataCache getInstance() {
        return _INSTANCE;
    }


    @VisibleForTesting
    public ConcurrentHashMap<CacheKey, Optional<GetFileDataQuery.Project>> getCacheProjectAnalysis() {
        return this.cacheProjectAnalysis;
    }

    @VisibleForTesting
    public ConcurrentHashMap<CacheKey, Optional<GetFileAnalysisQuery.GetFileAnalysis>> getCacheFileAnalysis() {
        return this.cacheFileAnalysis;
    }

    /**
     * Generate a unique hash for a file
     * @param input - the content of the file
     * @return
     */
    private String getFileHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            byte[] buffer = input.getBytes();
            md.update(buffer);
            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return hexStr;
        } catch (NoSuchAlgorithmException e){
            LOGGER.debug("impossible to get algorithm");
            return null;
        }
    }

    /**
     * Get the violation when inspecting a single file.
     * Look up in the cache and if not present, fetch the result from the API and show them.
     *
     * If the filename does not match with supported language by Code Inspector, it just returns
     * nothing as the API will not return anything for this file.
     *
     * @param projectId - the identifier of the project
     * @param filename - the filename
     * @param code - the code to analyze
     * @return
     */
    public Optional<GetFileAnalysisQuery.GetFileAnalysis> getViolationsFromFileAnalysis(Optional<Long> projectId, String filename, String code, Optional<String> parameters) throws GraphQlQueryException {
        String digest = getFileHash(code);
        CacheKey cacheKey = new CacheKey(projectId, null, filename, digest, parameters);
        LanguageEnumeration language = getLanguageFromFilename(filename);

        if (language == LanguageEnumeration.UNKNOWN) {
            LOGGER.debug(String.format("[AnalysisDataCache] Language unknown for file %s", filename));
            return Optional.empty();
        }

        LOGGER.debug(String.format("[AnalysisDataCache] Language file %s: %s", filename, language));

        if (!cacheFileAnalysis.containsKey(cacheKey)) {
            LOGGER.debug(String.format("[AnalysisDataCache] cache miss, fetching from API for key %s", cacheKey));
            Optional<GetFileAnalysisQuery.GetFileAnalysis> query = codeInspectorApi.getFileAnalysis(filename, code, language, projectId, parameters);
            cacheFileAnalysis.put(cacheKey, query);
        }

        return cacheFileAnalysis.get(cacheKey);
    }


    /**
     * Get violations from an existing project analysis on Code Inspector. It means
     * that this project has been analyzed on Code Inspector before and we fetch the results from the analysis.
     *
     * @param projectId - the identifier of the project
     * @param revision - the revision of the project
     * @param path - the path of the file to analyze
     * @return
     */
    public Optional<GetFileDataQuery.Project> getViolationsFromProjectAnalysis(Long projectId, String revision, String path) throws GraphQlQueryException {
        CacheKey cacheKey = new CacheKey(Optional.of(projectId), revision, path, null, null);
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
        this.cacheFileAnalysis.clear();
    }
}
