package io.codiga.plugins.jetbrains.actions.use_recipe;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.plugins.jetbrains.actions.CodeInsertionContext;
import io.codiga.plugins.jetbrains.ui.SearchPopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.math.BigDecimal;

import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;

public class UseRecipeSearchModel implements SearchPopup.Model {

    private final AnActionEvent anActionEvent;
    private final CodeInsertionContext codeInsertionContext;

    public UseRecipeSearchModel(AnActionEvent anActionEvent, CodeInsertionContext codeInsertionContext) {
        this.anActionEvent = anActionEvent;
        this.codeInsertionContext = codeInsertionContext;
    }

    @Override
    public @NotNull String getTitleText() {
        return "Codiga: Recipe Search";
    }

    @Override
    public @NotNull ListCellRenderer getListCellRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {

            if (value instanceof GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch){


                GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch recipe = (GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch) value;
                long recipeId = ((BigDecimal)recipe.id()).longValue();
                if(isSelected && (!codeInsertionContext.getCurrentRecipeId().isPresent() || codeInsertionContext.getCurrentRecipeId().get() != recipeId)){
                    removeAddedCode(anActionEvent, codeInsertionContext);
                    addRecipeToEditor(anActionEvent,
                        codeInsertionContext,
                        recipe.imports(),
                        recipe.jetbrainsFormat(),
                        recipe.language());
                    codeInsertionContext.setCurrentRecipeId(((BigDecimal)recipe.id()).longValue());

                }
                return new JLabel(recipe.name());
            }
            return new JLabel("unknown");
        };
    }

}
