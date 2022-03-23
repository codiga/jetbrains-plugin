package io.codiga.plugins.jetbrains.cache;

import io.codiga.api.type.LanguageEnumeration;

import java.util.List;
import java.util.Objects;


public class ShortcutCacheKey {
    private final LanguageEnumeration language;
    private final String filename;
    private final List<String> dependencies;

    public ShortcutCacheKey(LanguageEnumeration language, String filename, List<String> dependencies) {
        this.language = language;
        this.filename = filename;
        this.dependencies = dependencies;
    }


    @Override
    public String toString() {
        return "ShortcutCacheKey{" +
            "language=" + language.toString() +
            ", filename='" + filename + '\'' +
            ", dependencies='" + String.join(",", dependencies) + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortcutCacheKey cacheKey = (ShortcutCacheKey) o;
        return Objects.equals(language, cacheKey.language) &&
            Objects.equals(filename, cacheKey.filename) &&
            Objects.equals(dependencies, cacheKey.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, filename, dependencies);
    }


    public List<String> getDependencies() { return this.dependencies; };

    public LanguageEnumeration getLanguage() { return this.language; };

    public String getFilename() { return this.filename; };
}
