package io.codiga.plugins.jetbrains.cache;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.actions.ActionUtils.getLanguageFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;

/**
 * This is a listener used to refresh the cache of snippets each time a new file
 * is being opened.
 */
public class CacheRefreshEditorListener implements FileEditorManagerListener {

    private final Alarm alarm = new Alarm();

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        FileEditor fileEditor = event.getNewEditor();
        if (isNotNewFile(fileEditor)) {
            return;
        }

        alarm.cancelAllRequests();

        final int delay = 100;
        alarm.addRequest(() -> runBackgroundProcess(event), delay);
    }

    /**
     * If we close the editor, and we are not opening a new file, newFile is null.
     */
    private boolean isNotNewFile(@Nullable FileEditor fileEditor) {
        return fileEditor == null || fileEditor.getFile() == null;
    }

    private void runBackgroundProcess(@NotNull FileEditorManagerEvent event) {
        Project project = event.getManager().getProject();
        DumbService.getInstance(project).runReadActionInSmartMode(() -> refreshCache(project, event));
    }

    private void refreshCache(@NotNull Project project, @NotNull FileEditorManagerEvent event) {
        FileEditor fileEditor = event.getNewEditor();
        if (isNotNewFile(fileEditor)) {
            return;
        }

        String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, fileEditor.getFile());
        java.util.List<String> dependencies = DependencyManagement.getInstance().getDependencies(project, fileEditor.getFile()).stream().map(v -> v.getName()).collect(Collectors.toList());
        LanguageEnumeration languageEnumeration = getLanguageFromEditorForVirtualFile(fileEditor.getFile());
        ShortcutCacheKey shortcutCacheKey = new ShortcutCacheKey(languageEnumeration, filename, dependencies);
        ShortcutCache.getInstance().refreshCacheKey(shortcutCacheKey);
    }
}
