package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.ide.DataManager;
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
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.assistant.user_variables.UserVariables;
import io.codiga.plugins.jetbrains.cache.ShortcutCache;
import io.codiga.plugins.jetbrains.cache.ShortcutCacheKey;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.*;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getRelativeFilenamePathFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.utils.CodeImportUtils.hasImport;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;

/**
 * Provide completion when the user type some code on one line.
 *
 * We just take completion only when the line constraints only words (alphanumeric content).
 * That avoids triggering the completion all the time.
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
     * @param parameters
     * @param insertionContext
     */
    private void addRecipeInEditor(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe,
                                   int indentationCurrentLine,
                                   @NotNull CompletionParameters parameters,
                                   @NotNull InsertionContext insertionContext) {
        final String recipeJetBrainsFormat = recipe.jetbrainsFormat();
        final Long recipeId = ((BigDecimal)recipe.id()).longValue();
        final List<String> imports = recipe.imports();
        final LanguageEnumeration language = recipe.language();

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
        String unprocessedCode = new String(Base64.getDecoder().decode(recipeJetBrainsFormat))
            .replaceAll("\r\n", LINE_SEPARATOR);
        // DataContext is exposed easily in Actions, in other places like this, we need to look for it
        DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(context -> {
            final CodingAssistantContext CodigaTransformationContext = new CodingAssistantContext(context);
            // process supported variables dynamically
            final CodingAssistantCodigaTransform codingAssistantCodigaTransform = new CodingAssistantCodigaTransform(CodigaTransformationContext);
            String code = codingAssistantCodigaTransform.findAndTransformVariables(unprocessedCode);

            Template template = TemplateManagerImpl.getInstance(project).createTemplate(recipe.name(), recipe.name(), code);
            template.setToIndent(true);
            template.setToReformat(true);

            List<Variable> variables = UserVariables.getInstance().getVariablesFromCode(unprocessedCode);
            for (Variable variable: variables){
                template.addVariable(variable);
            }

            TemplateManagerImpl.getInstance(project).runTemplate(insertionContext.getEditor(), template);

            // Insert all imports
            try {
                WriteCommandAction.writeCommandAction(project).run(
                    (ThrowableRunnable<Throwable>) () -> {
                        int firstInsertion = firstPositionToInsert(currentCode, language);

                        for(String importStatement: imports) {
                            if(!hasImport(currentCode, importStatement, language)) {

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
        final boolean usesTabs = detectIfTabs(currentLine);
        final int indentationCurrentLine = getIndentation(currentLine, usesTabs);

        // clean tab indentation to correctly look for completion results
        currentLine = currentLine.replace("\t", "");


        int column = editor.getCaretModel().getCurrentCaret().getCaretModel().getVisualPosition().getColumn();


        Optional<String> keyword = getKeywordFromLine(currentLine, column - 1);


        if(!keyword.isPresent()) {
            return;
        }

        if(!keyword.get().startsWith(".") && !keyword.get().startsWith("/")) {
            return;
        }


        final VirtualFile virtualFile = parameters.getOriginalFile().getVirtualFile();
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());
        List<String> dependenciesName = dependencyManagement.getDependencies(parameters.getOriginalFile().getProject(), parameters.getOriginalFile().getVirtualFile())
          .stream().map(Dependency::getName)
          .collect(Collectors.toList());

        String filename = getRelativeFilenamePathFromEditorForVirtualFile(parameters.getOriginalFile().getProject(), parameters.getOriginalFile().getVirtualFile());

        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = ShortcutCache.getInstance().getRecipesShortcut(new ShortcutCacheKey(language, filename, dependenciesName));


        /*
         * We take only the top three recipes (they come ranked in order from the API).
         * For each of them, add a completion item and add a routine to insert the code.
         */
        for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe : recipes) {

          LookupElementBuilder element = LookupElementBuilder
            .create(recipe.name())
            .withTypeText(recipe.shortcut())
            .withCaseSensitivity(false)
            .withPresentableText(recipe.name())
            .withLookupString(recipe.shortcut())
            .withInsertHandler((insertionContext, lookupElement) ->
                    addRecipeInEditor(recipe, indentationCurrentLine, parameters, insertionContext))
            .withIcon(CodigaIcons.Codiga_default_icon);

          result.addElement(element);
        }
    }
}
