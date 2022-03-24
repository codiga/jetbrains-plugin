package io.codiga.plugins.jetbrains.cache;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.actions.ActionUtils.getLanguageFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;

/**
 * This is a listener used to refresh the cache of snippets each time a new file
 * is being opened.
 */
public class CacheRefreshEditorListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditor fileEditor = event.getNewEditor();

        /**
         * If we close the editor and we are not openning a new file,
         * newFile is null.
         */
        if (fileEditor == null) {
            return;
        }

        if (fileEditor.getFile() == null) {
            return;
        }
        Project project = event.getManager().getProject();
        String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, fileEditor.getFile());
        java.util.List<String> dependencies = DependencyManagement.getInstance().getDependencies(project, fileEditor.getFile()).stream().map(v -> v.getName()).collect(Collectors.toList());
        LanguageEnumeration languageEnumeration = getLanguageFromEditorForVirtualFile(fileEditor.getFile());
        ShortcutCacheKey shortcutCacheKey = new ShortcutCacheKey(languageEnumeration, filename, dependencies);
        ShortcutCache.getInstance().refreshCacheKey(shortcutCacheKey);
    }
}
