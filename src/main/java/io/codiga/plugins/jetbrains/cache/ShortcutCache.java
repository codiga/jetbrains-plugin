package io.codiga.plugins.jetbrains.cache;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import io.codiga.api.GetFileAnalysisQuery;
import io.codiga.api.GetFileDataQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.GraphQlQueryException;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;


public final class ShortcutCache {
    ConcurrentHashMap<ShortcutCacheKey, ShortcutCacheValue> cache;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private static ShortcutCache _INSTANCE = new ShortcutCache();

    private ShortcutCache() {
        cache = new ConcurrentHashMap<>();
    }

    public static ShortcutCache getInstance() {
        return _INSTANCE;
    }


    @VisibleForTesting
    public ConcurrentHashMap<ShortcutCacheKey, ShortcutCacheValue> getCache() {
        return this.cache;
    }



}
