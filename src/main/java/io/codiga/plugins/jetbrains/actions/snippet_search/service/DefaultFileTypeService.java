package io.codiga.plugins.jetbrains.actions.snippet_search.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Provides plain text file type in case no other language support needed for the snippet search is available.
 */
@Service(Service.Level.APP)
public final class DefaultFileTypeService implements SyntaxHighlightFileTypeService {
    @Override
    public @NotNull FileType getFileType() {
        return PlainTextFileType.INSTANCE;
    }
}
