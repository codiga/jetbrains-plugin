package io.codiga.plugins.jetbrains.actions.use_recipe;

import com.github.rjeschke.txtmark.Processor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameModelEx;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.CodigaMarkdownDecorator;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodeInsertion;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

import static io.codiga.plugins.jetbrains.Constants.LINE_SEPARATOR;
import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantUseRecipeAction extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        if (editor == null) {
            return;
        }

        CodeInsertionContext codeInsertionContext = new CodeInsertionContext();
        ChooseByNamePopup popup = ChooseByNamePopup.createPopup(event.getProject(), new RecipeChooseByNameModel(event, codeInsertionContext), new RecipeChooseByNameItemProvider(event, codeInsertionContext));

        popup.setAdText("Enter your search query to find snippets");
        popup.setSearchInAnyPlace(true);
        popup.setShowListForEmptyPattern(true);


        popup.invoke(new ChooseByNamePopupComponent.Callback() {
            @Override
            public void onClose() {
                removeAddedCode(event, codeInsertionContext);
            }

            @Override
            public void elementChosen(Object element) {

                if (element instanceof GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch) {
                    GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch recipe = (GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch) element;
                    long recipeId = ((BigDecimal)recipe.id()).longValue();

                    applyRecipe(event, recipeId, codeInsertionContext, codigaApi);
                }

                codeInsertionContext.clearAll();
            }
        }, ModalityState.current(), false);

    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();

        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
