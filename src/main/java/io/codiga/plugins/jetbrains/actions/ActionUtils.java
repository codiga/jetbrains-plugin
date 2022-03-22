package io.codiga.plugins.jetbrains.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ThrowableRunnable;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.use_recipe.CodeInsertionContext;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.CodeInsertion;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LINE_SEPARATOR;
import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.CodeImportUtils.hasImport;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.getIndentation;

public class ActionUtils {

    private ActionUtils() {
        // empty constructor to avoid building new object
    }

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public final static LanguageEnumeration getLanguageFromEditorForEvent(@NotNull AnActionEvent anActionEvent) {
        VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return LanguageEnumeration.UNKNOWN;
        }
        return(LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath()));
    }

    public final static LanguageEnumeration getLanguageFromEditorForVirtualFile(@NotNull VirtualFile virtualFile) {
        return(LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath()));
    }

    public final static String getFilenameFromEditorForEvent(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);

        if (psiFile == null || psiFile.getVirtualFile() == null) {
            return null;
        }

        return psiFile.getName();
    }

    public final static String getRelativeFilenamePathFromEditorForEvent(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);
        return getRelativeFilenamePathFromEditorForVirtualFile(psiFile.getProject(), psiFile.getVirtualFile());
    }

    public final static String getRelativeFilenamePathFromEditorForVirtualFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String canonicalPath = virtualFile.getCanonicalPath();
        String projectPath = project.getBasePath();

        String relativePath = canonicalPath.replace(projectPath, "");
        if(relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;

    }

    public final static List<String> getDependenciesFromEditorForEvent(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);
        DependencyManagement dependencyManagement = new DependencyManagement();
        return dependencyManagement.getDependencies(psiFile.getProject(), psiFile.getVirtualFile()).stream().map(d -> d.getName()).collect(Collectors.toList());
    }

    /**
     * Remove the code that was previously added when browsing a recipe.
     * Remove the added code from the editor.
     * @param anActionEvent
     */
    public static void removeAddedCode(AnActionEvent anActionEvent, List<CodeInsertion> codeInsertions, List<RangeHighlighter> highlighters) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            return;
        }
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();

        if (project == null) {
            LOGGER.info("showCurrentRecipe - editor, project or document is null");
            return;
        }

        if(!codeInsertions.isEmpty()) {
            try{
                WriteCommandAction.writeCommandAction(project).run(
                    (ThrowableRunnable<Throwable>) () -> {
                        int deletedLength = 0;
                        for (RangeHighlighter rangeHighlighter: highlighters) {
                            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
                        }
                        for(CodeInsertion codeInsertion: codeInsertions) {
                            document.deleteString(codeInsertion.getPositionStart() - deletedLength, codeInsertion.getPositionEnd() - deletedLength);
                            deletedLength = deletedLength + (codeInsertion.getPositionEnd() - codeInsertion.getPositionStart());
                        }

                        codeInsertions.clear();
                    }
                );
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the code that was previously added when browsing a recipe.
     * Remove the added code from the editor.
     * @param anActionEvent
     */
    public static void removeAddedCode(AnActionEvent anActionEvent, CodeInsertionContext context) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        if (editor == null) {
            return;
        }
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();

        if (project == null) {
            LOGGER.info("showCurrentRecipe - editor, project or document is null");
            return;
        }

        if(!context.getCodeInsertions().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    WriteCommandAction.writeCommandAction(project).run(
                        (ThrowableRunnable<Throwable>) () -> {
                            int deletedLength = 0;
                            for (RangeHighlighter rangeHighlighter : context.getHighlighters()) {
                                editor.getMarkupModel().removeHighlighter(rangeHighlighter);
                            }
                            for (CodeInsertion codeInsertion : context.getCodeInsertions()) {
                                document.deleteString(codeInsertion.getPositionStart() - deletedLength, codeInsertion.getPositionEnd() - deletedLength);
                                deletedLength = deletedLength + (codeInsertion.getPositionEnd() - codeInsertion.getPositionStart());
                            }

                            context.clearInsertions();
                        }
                    );
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public static void addRecipeToEditor(AnActionEvent anActionEvent,
                                         CodeInsertionContext codeInsertionContext,
                                         List<String> recipeImports,
                                         String recipeCodeJetBrainsFormat,
                                         LanguageEnumeration recipeLanguage) {
        addRecipeToEditor(anActionEvent, codeInsertionContext.getCodeInsertions(), codeInsertionContext.getHighlighters(), recipeImports, recipeCodeJetBrainsFormat, recipeLanguage);
    }

    public static void addRecipeToEditor(AnActionEvent anActionEvent,
                                         List<CodeInsertion> codeInsertions,
                                         List<RangeHighlighter> highlighters,
                                         List<String> recipeImports,
                                         String recipeCodeJetBrainsFormat,
                                         LanguageEnumeration recipeLanguage) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();
        String currentCode = document.getText();

        String unprocessedCode = new String(Base64.getDecoder().decode(recipeCodeJetBrainsFormat)).replaceAll("\r\n", LINE_SEPARATOR);
        final CodingAssistantContext CodigaTransformationContext = new CodingAssistantContext(anActionEvent.getDataContext());
        final CodingAssistantCodigaTransform codingAssistantCodigaTransform = new CodingAssistantCodigaTransform(CodigaTransformationContext);
        String code = codingAssistantCodigaTransform.findAndTransformVariables(unprocessedCode);

        // Get the current line and get the indentation
        int selectedLine = editor.getCaretModel().getVisualPosition().getLine();
        String currentLine = document.getText(new TextRange(document.getLineStartOffset(selectedLine), document.getLineEndOffset(selectedLine)));
        final boolean usesTabs = detectIfTabs(currentLine);
        int indentationCurrentLine = getIndentation(currentLine, usesTabs);

        String indentedCode = indentOtherLines(code, indentationCurrentLine, usesTabs);

        ApplicationManager.getApplication().invokeLater(() -> {
            // add the code and update global variables to indicate code has been inserted.
            try {
                WriteCommandAction.writeCommandAction(project, psiFile).run(
                    (ThrowableRunnable<Throwable>) () -> {
                        int editorOffset = editor.getCaretModel().getOffset();
                        int firstInsertion = firstPositionToInsert(currentCode, recipeLanguage);
                        int lengthInsertedForImport = 0;

                        for (String importStatement : recipeImports) {
                            if (!hasImport(currentCode, importStatement, recipeLanguage)) {
                                String dependencyStatement = importStatement + LINE_SEPARATOR;
                                codeInsertions.add(new CodeInsertion(
                                    dependencyStatement,
                                    firstInsertion + lengthInsertedForImport,
                                    firstInsertion + lengthInsertedForImport + dependencyStatement.length()));
                                lengthInsertedForImport = lengthInsertedForImport + dependencyStatement.length();
                            }
                        }

                        int startOffset = editorOffset + lengthInsertedForImport;
                        int endOffset = startOffset + indentedCode.length();
                        codeInsertions.add(new CodeInsertion(indentedCode, startOffset, endOffset));

                        for (CodeInsertion codeInsertion: codeInsertions) {
                            document.insertString(codeInsertion.getPositionStart(), codeInsertion.getCode());

                            RangeHighlighter newHighlighter = editor.getMarkupModel()
                                .addRangeHighlighter(codeInsertion.getPositionStart(), codeInsertion.getPositionStart() + codeInsertion.getCode().length(), 0,
                                    new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.PINK, EffectType.ROUNDED_BOX, 13),
                                    HighlighterTargetArea.EXACT_RANGE);
                            highlighters.add(newHighlighter);
                        }
                    }
                );
            } catch (Throwable e) {
                e.printStackTrace();
                LOGGER.error("showCurrentRecipe - impossible to update the code from the recipe");
                LOGGER.error(e);
            }
        });

    }

    /**
     * Apply the recipe. We send a callback to the API and
     * remove all highlighted code in the editor.
     *
     * @param anActionEvent
     */
    public static void applyRecipe(AnActionEvent anActionEvent,
                                   Long recipeId,
                                   List<CodeInsertion> codeInsertions,
                                   List<RangeHighlighter> highlighters,
                                   CodigaApi codigaApi) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            LOGGER.warn("applyRecipe - editor is null");
            return;
        }
        codigaApi.recordRecipeUse(recipeId);
        codeInsertions.clear();
        // remove the highlighted code.
        for (RangeHighlighter rangeHighlighter : highlighters) {
            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        }
        highlighters.clear();
    }

    /**
     * Apply the recipe. We send a callback to the API and
     * remove all highlighted code in the editor.
     *
     * TODO: remove this part
     *
     * @param anActionEvent
     */
    public static void applyRecipe(AnActionEvent anActionEvent,
                                   Long recipeId,
                                   CodeInsertionContext codeInsertionContext,
                                   CodigaApi codigaApi) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            LOGGER.warn("applyRecipe - editor is null");
            return;
        }
        codigaApi.recordRecipeUse(recipeId);
        codeInsertionContext.getCodeInsertions().clear();
        // remove the highlighted code.
        for (RangeHighlighter rangeHighlighter : codeInsertionContext.getHighlighters()) {
            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        }
        codeInsertionContext.clearAll();
    }

}
