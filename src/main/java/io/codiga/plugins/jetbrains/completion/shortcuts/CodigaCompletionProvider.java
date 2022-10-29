package io.codiga.plugins.jetbrains.completion.shortcuts;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.StartOnlyMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.cache.ShortcutCache;
import io.codiga.plugins.jetbrains.cache.ShortcutCacheKey;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;
import static io.codiga.plugins.jetbrains.utils.RecipeUtils.addRecipeInEditor;

/**
 * Provide completion when the user type some code on one line.
 * <p>
 * We just take completion only when the line constraints only words (alphanumeric content).
 * That avoids triggering the completion all the time.
 */
public class CodigaCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final DependencyManagement dependencyManagement = new DependencyManagement();
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    CodigaCompletionProvider() {
    }

    /**
     * Add the completion: call the API to get all completions and surface them
     *
     * @param parameters
     * @param context
     * @param result
     */
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        LOGGER.debug("Triggering completion");

        if (!AppSettingsState.getInstance().getCodigaEnabled()) {
            LOGGER.debug("codiga disabled");
            return;
        }

        if (!AppSettingsState.getInstance().getUseCompletion()) {
            LOGGER.debug("completion deactivated");
            return;
        }

        final Editor editor = parameters.getEditor();
        final int lineStart = editor.getCaretModel().getVisualLineStart();
        final int lineEnd = editor.getCaretModel().getVisualLineEnd();

        String currentLine = "";
        if (lineEnd > lineStart) {
            currentLine = editor.getDocument().getText(new TextRange(lineStart, lineEnd));
        }

        // clean tab indentation to correctly look for completion results
        currentLine = currentLine.replace("\t", "");

        int column = editor.getCaretModel().getCurrentCaret().getCaretModel().getVisualPosition().getColumn();

        // we are not attempting to always autocomplete, especially if the user is attempting to invoke a method
        if (!shouldAutocomplete(currentLine, column - 1)) {
            return;
        }

        // Attempt to get the keyword and if not present, just exit.
        Optional<String> keyword = getKeywordFromLine(currentLine, column - 1)
            // The keyword has to start with a . or a slash
            .filter(kw -> kw.startsWith(".") || kw.startsWith("/"));

        if (keyword.isEmpty()) {
            return;
        }

        keyword = keyword
            //If the text entered is only a dot or slash, put no keyword so that we search all shortcuts
            .filter(kw -> !kw.equalsIgnoreCase(".") && !kw.equalsIgnoreCase("/"))
            //If the keyword is longer than one character and starts with a dot, remove the dot so that
            // we filter by the correct prefix.
            .filter(kw -> kw.length() > 1 && (kw.startsWith(".") || kw.startsWith("/")))
            .map(kw -> kw.substring(1));


        Project project = parameters.getOriginalFile().getProject();
        final VirtualFile virtualFile = parameters.getOriginalFile().getVirtualFile();
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

        List<String> dependenciesName = dependencyManagement.getDependencies(project, virtualFile)
            .stream().map(Dependency::getName)
            .collect(Collectors.toList());

        String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, virtualFile);
        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = ShortcutCache.getInstance().getRecipesShortcut(new ShortcutCacheKey(language, filename, dependenciesName));

        final boolean usesTabs = detectIfTabs(currentLine);
        final int indentationCurrentLine = getIndentation(currentLine, usesTabs);

        List<LookupElementBuilder> elements = new ArrayList<>();

        for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe : recipes) {
            if (keyword.isPresent() && !recipe.shortcut().startsWith(keyword.get())) {
                continue;
            }

            LookupElementBuilder element = LookupElementBuilder
                .create(recipe.name())
                .withTypeText(recipe.name())
                .withCaseSensitivity(false)
                .withPresentableText(String.format("%s ⇌", recipe.shortcut()))
                .withLookupString(recipe.shortcut())
                .withInsertHandler((insertionContext, lookupElement) ->
                    addRecipeInEditor(
                        insertionContext.getEditor(),
                        recipe.name(),
                        recipe.jetbrainsFormat(),
                        ((BigDecimal) recipe.id()).longValue(),
                        recipe.imports(),
                        recipe.language(),
                        indentationCurrentLine,
                        true,
                        codigaApi))
                .withIcon(CodigaIcons.Codiga_default_icon);


            elements.add(element);
        }

        // Match only based on prefix.
        result.withPrefixMatcher(new StartOnlyMatcher(result.getPrefixMatcher())).addAllElements(elements);
    }
}
