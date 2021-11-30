package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.*;

/**
 * Provide completion when the user type some code on one line.
 *
 * We just take completion only when the line containts only words (alphanumeric content).
 * That avoids to trigger the completion all the time.
 *
 */
public class CodigaCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final DependencyManagement dependencyManagement = new DependencyManagement();
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    CodigaCompletionProvider() {
    }

    /**
     * Add the completion: call the API to get all completions and surface them
     * @param parameters
     * @param context
     * @param result
     */
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        LOGGER.debug("Triggering completion");

        if(!AppSettingsState.getInstance().getUseCompletion()) {
            LOGGER.debug("completion deactivated");
            return;
        }

        final Editor editor = parameters.getEditor();
        final int lineStart = editor.getCaretModel().getVisualLineStart();
        final int lineEnd = editor.getCaretModel().getVisualLineEnd();

        String currentLine = "";
        if(lineEnd > lineStart + 1){
            currentLine = editor.getDocument().getText(new TextRange(lineStart, lineEnd - 1));
        }


        if (currentLine.length() < MINIMUM_LINE_LENGTH_TO_TRIGGER_AUTOCOMPLETION){
            LOGGER.debug(String.format("string too small |%s|", currentLine));
            return;
        }

        if (!currentLine.chars().allMatch(c -> Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
            LOGGER.debug(String.format("not all characters are alphanumeric for String |%s|", currentLine));
            return;
        }

        // Get all recipes parameters.
        final List<String> keywords = Arrays.asList(currentLine.split(" "));
        final VirtualFile virtualFile = parameters.getOriginalFile().getVirtualFile();
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());
        List<String> dependenciesName = dependencyManagement.getDependencies(parameters.getOriginalFile()).stream().map(d -> d.getName()).collect(Collectors.toList());
        final String filename = virtualFile.getName();

        // Get the recipes from the API.
        List<GetRecipesForClientQuery.GetRecipesForClient> recipes = codigaApi.getRecipesForClient(
            keywords,
            dependenciesName,
            Optional.empty(),
            language,
            filename);


        LOGGER.debug(String.format("found %s recipes", recipes.size()));

        /**
         * We take only the top three recipes (they come ranked in order from the API).
         * For each of them, add a completion item and add a routine to insert the code.
         */
        for(GetRecipesForClientQuery.GetRecipesForClient recipe: recipes.stream().limit(NUMBER_OF_RECIPES_TO_KEEP_FOR_COMPLETION).collect(Collectors.toList())){
            String lookup = String.join(" ", recipe.keywords());

            LookupElementBuilder element = LookupElementBuilder
                .create(lookup)
                .withTypeText(String.join(",", recipe.keywords()))
                .withLookupString(lookup)
                .withInsertHandler((insertionContext, lookupElement) -> {
                    insertionContext.setAddCompletionChar(false);

                    // remove the code on the line
                    final int startOffetToRemove = insertionContext.getEditor().getCaretModel().getVisualLineStart();
                    final int endOffetToRemove = insertionContext.getEditor().getCaretModel().getVisualLineEnd();
                    insertionContext.getEditor().getDocument().deleteString(startOffetToRemove, endOffetToRemove );

                    // add the code and update the document.
                    String code = new String(Base64.getDecoder().decode(recipe.code())).replaceAll("\r\n", LINE_SEPARATOR);
                    EditorModificationUtil.insertStringAtCaret(insertionContext.getEditor(), code);
                    insertionContext.commitDocument();

                    // sent a callback that the recipe has been used.
                    long recipeId = ((BigDecimal) recipe.id()).longValue();
                    codigaApi.recordRecipeUse(recipeId);
                })
                .withIcon(CodigaIcons.Codiga_default_icon);

            result.addElement(element);
        }
    }
}
