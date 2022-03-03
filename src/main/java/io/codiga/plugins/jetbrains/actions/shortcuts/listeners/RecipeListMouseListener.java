package io.codiga.plugins.jetbrains.actions.shortcuts.listeners;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.ui.components.JBList;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodeInsertion;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.applyRecipe;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.removeAddedCode;

public class RecipeListMouseListener implements MouseListener {

    private JBList jbList;
    private AnActionEvent anActionEvent;
    private List<CodeInsertion> codeInsertions;
    private List<RangeHighlighter> highlighters;
    private WindowWrapper windowWrapper;
    private CodigaApi codigaApi;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public RecipeListMouseListener(AnActionEvent anActionEvent,
                                   JBList jbList,
                                   List<CodeInsertion> codeInsertions,
                                   List<RangeHighlighter> highlighters,
                                   WindowWrapper windowWrapper,
                                   CodigaApi codigaApi) {
        this.jbList = jbList;
        this.anActionEvent = anActionEvent;
        this.codeInsertions = codeInsertions;
        this.highlighters = highlighters;
        this.windowWrapper = windowWrapper;
        this.codigaApi = codigaApi;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1){
            GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut selectedRecipe = (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut)jbList.getSelectedValue();
            if (selectedRecipe == null) {
                LOGGER.debug("[SearchKeyListener] value is null");
                return;
            }
            long recipeId = ((BigDecimal) selectedRecipe.id()).longValue();
            applyRecipe(anActionEvent, recipeId, codeInsertions, highlighters, codigaApi);
            windowWrapper.close();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}