package io.codiga.plugins.jetbrains.actions.shortcuts.model;

import io.codiga.api.GetRecipesForClientByShortcutQuery;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecipeListModel implements ListModel {
    List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = new ArrayList<>();

    public RecipeListModel(List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> newRecipes) {
        this.recipes.clear();
        this.recipes.addAll(newRecipes);
        sortList();
    }


    public void setRecipes(List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> newRecipes){
        recipes = newRecipes;
    }


    @Override
    public int getSize() {
        return recipes.size();
    }

    @Override
    public Object getElementAt(int index) {
        return recipes.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }


    public boolean hasRecipe(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
        return recipes.contains(recipe);
    }


    public boolean remove(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
        return recipes.remove(recipe);
    }

    public boolean add(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
        return recipes.add(recipe);
    }

    public void sortList() {
        recipes.sort(new Comparator<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut>() {
            @Override
            public int compare(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut o1, GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut o2) {
                return o1.shortcut().compareTo(o2.shortcut());
            }
        });
    }
}