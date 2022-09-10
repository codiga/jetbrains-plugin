package io.codiga.plugins.jetbrains.utils;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ThrowableRunnable;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.assistant.user_variables.UserVariables;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LINE_SEPARATOR;
import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.CodeImportUtils.hasImport;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.firstPositionToInsert;

public final class RecipeUtils {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private RecipeUtils() {};


    public static void addRecipeInEditor(@NotNull Editor editor,
                                         @NotNull String recipeName,
                                         @NotNull String jetBrainsFormat,
                                         @NotNull Long recipeId,
                                         @NotNull List<String> imports,
                                         @NotNull LanguageEnumeration language,
                                         int indentationCurrentLine,
                                         boolean removeCurrentLine,
                                         @NotNull CodigaApi codigaApi) {


        final Document document = editor.getDocument();
        final String currentCode = document.getText();
        final Project project = editor.getProject();

        if (project == null) {
            LOGGER.info("project is null");
            return;
        }

        if (removeCurrentLine) {
            // remove the code on the line
            int startOffsetToRemove = editor.getCaretModel().getVisualLineStart();
            final int endOffsetToRemove = editor.getCaretModel().getVisualLineEnd();
            editor.getDocument()
                .deleteString(startOffsetToRemove + indentationCurrentLine, endOffsetToRemove );
        }


        // add the code and update the document.
        String unprocessedCode = new String(Base64.getDecoder().decode(jetBrainsFormat))
            .replaceAll("\r\n", LINE_SEPARATOR);
        // DataContext is exposed easily in Actions, in other places like this, we need to look for it
        DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());

        final CodingAssistantContext codigaTransformationContext = new CodingAssistantContext(dataContext);

        if (!codigaTransformationContext.isValid()) {
            LOGGER.info("context is not valid");
            return;
        }

        // process supported variables dynamically
        final CodingAssistantCodigaTransform codingAssistantCodigaTransform = new CodingAssistantCodigaTransform(codigaTransformationContext);
        String code = codingAssistantCodigaTransform.findAndTransformVariables(unprocessedCode, dataContext);

        // For mysterious reasons, we need to add a newline to the code to not take the next line.
        Template template = TemplateManagerImpl.getInstance(project).createTemplate(recipeName, recipeName, code + "\n");
        template.setToIndent(true);
        template.setToReformat(true);

        List<Variable> variables = UserVariables.getInstance().getVariablesFromCode(unprocessedCode);
        for (Variable variable: variables){
            template.addVariable(variable);
        }


        // Insert all imports
        try {
            TemplateManager.getInstance(project).runTemplate(editor, template);


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
    }
}
