package io.codiga.plugins.jetbrains.actions.shortcuts.listeners;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.model.CodeInsertion;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
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
    private JButton learnMoreButton;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public RecipeListSelectionListener(JBList jbList, AnActionEvent anActionEvent,
                                       List<CodeInsertion> codeInsertions,
                                       List<RangeHighlighter> highlighters,
                                       JButton learnMoreButton) {
        this.jbList = jbList;
        this.anActionEvent = anActionEvent;
        this.codeInsertions = codeInsertions;
        this.highlighters = highlighters;
        this.learnMoreButton = learnMoreButton;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut selectedRecipe = (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut)jbList.getSelectedValue();
        if (selectedRecipe == null) {
            learnMoreButton.setEnabled(false);
            LOGGER.debug("[SearchKeyListener] value is null");
            return;
        }
        if(!codeInsertions.isEmpty()) {
            removeAddedCode(anActionEvent, codeInsertions, highlighters);
        }
        addRecipeToEditor(anActionEvent, codeInsertions, highlighters, selectedRecipe.imports(), selectedRecipe.jetbrainsFormat(), selectedRecipe.language());
        long recipeId = ((BigDecimal)selectedRecipe.id()).longValue();
        String url = String.format("https://app.codiga.io/hub/recipe/%s/view", recipeId);
        for(ActionListener actionListener: learnMoreButton.getActionListeners()){
            learnMoreButton.removeActionListener(actionListener);
        }
        learnMoreButton.setEnabled(true);
        learnMoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    Desktop.getDesktop().browse(new URI(url));

                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });


    }
}