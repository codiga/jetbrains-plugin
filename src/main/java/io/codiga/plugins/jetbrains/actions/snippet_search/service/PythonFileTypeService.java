package io.codiga.plugins.jetbrains.actions.snippet_search.service;

import com.intellij.openapi.fileTypes.FileType;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Provides Python file type for the syntax highlighting in the snippet search tool window.
 */
public class PythonFileTypeService implements SyntaxHighlightFileTypeService {
    @Override
    public @NotNull FileType getFileType() {
        return PythonFileType.INSTANCE;
    }
}
