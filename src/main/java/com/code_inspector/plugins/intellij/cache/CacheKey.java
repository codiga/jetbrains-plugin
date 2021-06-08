package com.code_inspector.plugins.intellij.cache;

import java.util.Objects;

/**
 * This is the key used to cache data in a HashMap. It used the unique combination
 * of project id, revision and filename. This class should not be used in the code
 * and only be used in AnalysisDataCache.
 */
public class CacheKey {
    private final Long projectId;
    private final String revision;
    private final String filename;
    private final String digest;

    public CacheKey(Long projectId, String revision, String filename, String digest) {
        this.projectId = projectId;
        this.revision = revision;
        this.filename = filename;
        this.digest = digest;
    }


    @Override
    public String toString() {
        return "CacheKey{" +
            "projectId=" + projectId +
            ", revision='" + revision + '\'' +
            ", digest='" + digest + '\'' +
            ", filename='" + filename + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(projectId, cacheKey.projectId) &&
            Objects.equals(digest, cacheKey.digest) &&
            Objects.equals(revision, cacheKey.revision) &&
            Objects.equals(filename, cacheKey.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, revision, filename, digest);
    }
}
