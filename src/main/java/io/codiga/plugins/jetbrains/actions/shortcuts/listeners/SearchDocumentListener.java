package io.codiga.plugins.jetbrains.actions.shortcuts.listeners;

import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.actions.shortcuts.model.RecipeListModel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;

/**
 * In the shortcut finder, we sort/search the shortcuts by prefix. This listener
 * listens to the changes in the document and filter the list of element in the
 * shortcuts list.
 */
public class SearchDocumentListener implements DocumentListener {
    private JBTextField jbTextField;
    private JBList jbList;
    private List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> allRecipes;

    public SearchDocumentListener(JBList jbList, JBTextField jbTextField, List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> allRecipes){
        this.jbList = jbList;
        this.jbTextField = jbTextField;
        this.allRecipes = allRecipes;
    }

    private void filterElements() {
        RecipeListModel model = (RecipeListModel)jbList.getModel();
        String term = jbTextField.getText().toLowerCase();
        if(term.isEmpty()) {
            for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe: allRecipes) {
                if(!model.hasRecipe(recipe)) {
                    model.add(recipe);
                }
            }
        } else {
            for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe: allRecipes) {
                boolean recipeMatch = recipe.name().toLowerCase().contains(term) || recipe.shortcut().toLowerCase().startsWith(term);
                if(recipeMatch) {
                    if(!model.hasRecipe(recipe)){
                        model.add(recipe);
                    }
                } else {
                    if (model.hasRecipe(recipe)){
                        model.remove(recipe);
                    }
                }
            }
        }
        if(jbList.getItemsCount() > 0) {
            jbList.setSelectedIndex(0);
        }
        model.sortList();
        jbList.repaint();

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        filterElements();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        filterElements();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }
}