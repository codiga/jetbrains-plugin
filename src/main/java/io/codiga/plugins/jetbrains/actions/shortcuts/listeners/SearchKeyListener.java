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
import java.math.BigDecimal;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.applyRecipe;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.removeAddedCode;

public class SearchKeyListener implements KeyListener {

    private JBList jbList;
    private AnActionEvent anActionEvent;
    private List<CodeInsertion> codeInsertions;
    private List<RangeHighlighter> highlighters;
    private WindowWrapper windowWrapper;
    private CodigaApi codigaApi;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public SearchKeyListener(AnActionEvent anActionEvent,
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
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_PAGE_UP) { // 38 is up arrow key ; 33 is page up
            int newIndex = jbList.getSelectedIndex() - 1;
            if (newIndex >= 0) {
                jbList.setSelectedIndex(newIndex);
            }
            jbList.ensureIndexIsVisible(jbList.getSelectedIndex());
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) { // 40 down arrow key ; 34 is page down
            int newIndex = jbList.getSelectedIndex() + 1;
            if (newIndex < jbList.getItemsCount()) {
                jbList.setSelectedIndex(newIndex);
            }

            jbList.ensureIndexIsVisible(jbList.getSelectedIndex());
        }

        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if(!codeInsertions.isEmpty()) {
                removeAddedCode(anActionEvent, codeInsertions, highlighters);
            }
            codeInsertions.clear();
            windowWrapper.close();
        }

        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
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
    public void keyReleased(KeyEvent e) {

    }
}