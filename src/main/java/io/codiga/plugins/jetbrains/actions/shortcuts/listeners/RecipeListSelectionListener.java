package io.codiga.plugins.jetbrains.actions.shortcuts.listeners;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.ui.components.JBList;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.model.CodeInsertion;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.addRecipeToEditor;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.removeAddedCode;

/**
 * Listener for the list of recipes being shown. It updates the current editor with
 * the recipe inside based on what is being shown.
 */
public class RecipeListSelectionListener implements ListSelectionListener {
    private JBList jbList;
    private AnActionEvent anActionEvent;
    private List<CodeInsertion> codeInsertions;
    private List<RangeHighlighter> highlighters;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public RecipeListSelectionListener(JBList jbList, AnActionEvent anActionEvent, List<CodeInsertion> codeInsertions, List<RangeHighlighter> highlighters) {
        this.jbList = jbList;
        this.anActionEvent = anActionEvent;
        this.codeInsertions = codeInsertions;
        this.highlighters = highlighters;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut selectedRecipe = (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut)jbList.getSelectedValue();
        if (selectedRecipe == null) {
            LOGGER.debug("[SearchKeyListener] value is null");
            return;
        }
        if(!codeInsertions.isEmpty()) {
            removeAddedCode(anActionEvent, codeInsertions, highlighters);
        }
        addRecipeToEditor(anActionEvent, codeInsertions, highlighters, selectedRecipe.imports(), selectedRecipe.jetbrainsFormat(), selectedRecipe.language());
    }
}