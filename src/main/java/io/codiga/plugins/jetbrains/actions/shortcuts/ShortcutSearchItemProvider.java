package io.codiga.plugins.jetbrains.actions.shortcuts;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.ui.SearchPopup;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;

public class ShortcutSearchItemProvider implements SearchPopup.SearchItemProvider {

    private final AnActionEvent anActionEvent;
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    public ShortcutSearchItemProvider(AnActionEvent anActionEvent) {
        this.anActionEvent = anActionEvent;
    }

    @Override
    public void filterElements(@NotNull SearchPopup.Model model, @NotNull String s, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<Object> processor) {

        String toSearch = s.isEmpty() ? null : s;

        if (!progressIndicator.isRunning()) {
            progressIndicator.start();

        }

        String filename = getUnixRelativeFilenamePathFromEditorForEvent(anActionEvent);
        List<String> dependenciesName = getDependenciesFromEditorForEvent(anActionEvent);
        LanguageEnumeration language = getLanguageFromEditorForEvent(anActionEvent);

        if (language == LanguageEnumeration.UNKNOWN) {
            return;
        }

        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> newRecipes = codigaApi.getRecipesForClientByShotcurt(
            Optional.ofNullable(toSearch),
            dependenciesName,
            Optional.empty(),
            language,
            filename
        );

        newRecipes.forEach(processor::process);

        if (progressIndicator.isRunning() && !progressIndicator.isCanceled()) {
            progressIndicator.stop();
        }

    }
}
