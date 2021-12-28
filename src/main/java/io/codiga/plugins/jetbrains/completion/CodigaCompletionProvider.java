package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.ThrowableRunnable;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.*;
import static io.codiga.plugins.jetbrains.utils.CodeImportUtils.hasImport;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;

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
     * Add the recipe into the editor.
     * @param recipe
     * @param indentationCurrentLine
     * @param parameters
     * @param insertionContext
     */
    private void addRecipeInEditor(GetRecipesForClientQuery.GetRecipesForClient recipe,
                                   int indentationCurrentLine,
                                   @NotNull CompletionParameters parameters,
                                   @NotNull InsertionContext insertionContext,
                                   Boolean usesTabs) {
        insertionContext.setAddCompletionChar(false);
        final Editor editor = parameters.getEditor();
        final Document document = editor.getDocument();
        final String currentCode = document.getText();
        final Project project = parameters.getEditor().getProject();

        // remove the code on the line
        int startOffsetToRemove = insertionContext.getEditor().getCaretModel().getVisualLineStart();
        final int endOffsetToRemove = insertionContext.getEditor().getCaretModel().getVisualLineEnd();
        insertionContext.getEditor().getDocument()
          .deleteString(startOffsetToRemove + indentationCurrentLine, endOffsetToRemove );

        // add the code and update the document.
        String unprocessedCode = new String(Base64.getDecoder().decode(recipe.jetbrainsFormat()))
          .replaceAll("\r\n", LINE_SEPARATOR);
        // DataContext is exposed easily in Actions, in other places like this, we need to look for it
        DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(context -> {
          final CodingAssistantContext CodigaTransformationContext = new CodingAssistantContext(context);
          // process supported variables dynamically
          final CodingAssistantCodigaTransform codingAssistantCodigaTransform = new CodingAssistantCodigaTransform(CodigaTransformationContext);
          String code = codingAssistantCodigaTransform.findAndTransformVariables(unprocessedCode);
          String indentedCode = indentOtherLines(code, indentationCurrentLine, usesTabs) + "\n";

          /**
           * Insert the code
           */
          EditorModificationUtil.insertStringAtCaret(insertionContext.getEditor(), indentedCode);
          insertionContext.commitDocument();

          /**
           * Insert all imports
           */
          List<String> imports = recipe.imports();
          try {

              WriteCommandAction.writeCommandAction(project).run(
                  (ThrowableRunnable<Throwable>) () -> {
                      int firstInsertion = firstPositionToInsert(currentCode, recipe.language());

                      for(String importStatement: imports) {
                          if(!hasImport(currentCode, importStatement, recipe.language())) {

                              String dependencyStatement = importStatement + LINE_SEPARATOR;
                              document.insertString(firstInsertion, dependencyStatement);
                          }
                      }
                  }
              );
          } catch (Throwable e) {
              e.printStackTrace();
              LOGGER.error("showCurrentRecipe - impossible to update the code from the recipe");
              LOGGER.error(e);
          }

          // sent a callback that the recipe has been used.
          long recipeId = ((BigDecimal) recipe.id()).longValue();
          codigaApi.recordRecipeUse(recipeId);
        });
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
        final boolean usesTabs = detectIfTabs(currentLine);
        final int indentationCurrentLine = usesTabs
          ? getIndentation(currentLine, true)
          : getIndentation(currentLine, false);

        // clean tab indentation to correctly look for completion results
        currentLine = currentLine.replace("\t", "");

        if (currentLine.length() < MINIMUM_LINE_LENGTH_TO_TRIGGER_AUTOCOMPLETION){
            LOGGER.debug(String.format("string too small |%s|", currentLine));
            return;
        }

        if (!currentLine.chars().allMatch(c -> Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
            LOGGER.debug(String.format("not all characters are alphanumeric for String |%s|", currentLine));
            return;
        }

        // Get all recipes parameters.
        final List<String> keywords = Arrays.asList(currentLine.split(" ")).stream().filter(p -> !p.isEmpty())
          .collect(Collectors.toList());
        final VirtualFile virtualFile = parameters.getOriginalFile().getVirtualFile();
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());
        List<String> dependenciesName = dependencyManagement.getDependencies(parameters.getOriginalFile())
          .stream().map(d -> d.getName())
          .collect(Collectors.toList());
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
        for (GetRecipesForClientQuery.GetRecipesForClient recipe : recipes.stream()
          .limit(NUMBER_OF_RECIPES_TO_KEEP_FOR_COMPLETION).collect(Collectors.toList())) {
          List<String> recipeKeywords = new ArrayList<>(recipe.keywords());
          if (recipe.shortcut() != null) {
            recipeKeywords.add(recipe.shortcut());
          }


          String lookup = String.join(" ", recipeKeywords);

          LookupElementBuilder element = LookupElementBuilder
            .create(recipe.name())
            .withTypeText(String.join(",", recipeKeywords))
            .withLookupString(lookup)
            .withInsertHandler((insertionContext, lookupElement) -> {
              addRecipeInEditor(recipe, indentationCurrentLine, parameters, insertionContext, usesTabs);
            })
            .withIcon(CodigaIcons.Codiga_default_icon);


          result.addElement(element);
        }
    }
}
