package io.codiga.plugins.jetbrains.cache;

import java.util.Objects;
import java.util.Optional;

/**
 * This is the key used to cache data in a HashMap. It used the unique combination
 * of project id, revision and filename. This class should not be used in the code
 * and only be used in AnalysisDataCache.
 */
public class CacheKey {
    private final Optional<Long> projectId;
    private final String revision;
    private final String filename;
    private final String digest;
    private final Optional<String> parameters;

    public CacheKey(Optional<Long> projectId, String revision, String filename, String digest, Optional<String> parameters) {
        this.projectId = projectId;
        this.revision = revision;
        this.filename = filename;
        this.digest = digest;
        this.parameters = parameters;
    }


    @Override
    public String toString() {
        return "CacheKey{" +
            "projectId=" + projectId +
            ", revision='" + revision + '\'' +
            ", digest='" + digest + '\'' +
            ", filename='" + filename + '\'' +
            ", parameters='" + parameters + '\'' +
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
            Objects.equals(parameters, cacheKey.parameters) &&
            Objects.equals(filename, cacheKey.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, revision, filename, digest);
    }
}
