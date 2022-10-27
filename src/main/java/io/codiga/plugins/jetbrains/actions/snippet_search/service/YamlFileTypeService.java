package io.codiga.plugins.jetbrains.actions.snippet_search.service;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

/**
 * Provides YAML file type for the syntax highlighting in the snippet search tool window.
 */
public class YamlFileTypeService implements SyntaxHighlightFileTypeService {
    @Override
    public @NotNull FileType getFileType() {
        return YAMLFileType.YML;
    }
}
