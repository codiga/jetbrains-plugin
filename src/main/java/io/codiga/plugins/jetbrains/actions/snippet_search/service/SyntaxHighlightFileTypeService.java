package io.codiga.plugins.jetbrains.actions.snippet_search.service;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * Provides file type for the snippet search tool window based on which the displayed code snippets are
 * syntax highlighted.
 * <p>
 * This service based file type retrieval logic is put in place to hide the concrete file type classes,
 * so we don't encounter {@code NoClassDefFoundException}s in case a language support plugin is disabled or
 * not installed in the IDE.
 */
public interface SyntaxHighlightFileTypeService {

    /**
     * Returns the file type based on which syntax highlighting is applied.
     */
    @NotNull
    FileType getFileType();
}
