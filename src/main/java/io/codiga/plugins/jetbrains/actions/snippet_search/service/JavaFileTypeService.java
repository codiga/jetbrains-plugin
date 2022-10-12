package io.codiga.plugins.jetbrains.actions.snippet_search.service;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * Provides Java file type for the syntax highlighting in the snippet search tool window.
 */
public class JavaFileTypeService implements SyntaxHighlightFileTypeService {
    @Override
    public @NotNull FileType getFileType() {
        return JavaFileType.INSTANCE;
    }
}
