package io.codiga.plugins.jetbrains.actions.shortcuts;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.plugins.jetbrains.actions.CodeInsertionContext;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.ui.SearchPopup;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantListShortcutsAction extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        if (editor == null) {
            return;
        }


        CodeInsertionContext codeInsertionContext = new CodeInsertionContext();
        SearchPopup popup = new SearchPopup(
                event.getProject(),
                new ShortcutChooseByNameModel(event, codeInsertionContext),
                new ShortcutSearchItemProvider(event)
        );
        popup.invoke(new SearchPopup.Callback() {
            @Override
            public void onClose() {
                removeAddedCode(event, codeInsertionContext);
            }

            @Override
            public void elementChosen(Object element) {

                if (element instanceof GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut) {
                    GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe = (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut) element;

                    applyRecipe(event,
                            recipe.name(),
                            recipe.jetbrainsFormat(),
                            ((BigDecimal)recipe.id()).longValue(),
                            recipe.imports(),
                            recipe.language(),
                            codeInsertionContext,
                            codigaApi);

                }

                codeInsertionContext.clearAll();
            }
        });
    }

    @Override
    public void update(AnActionEvent e) {
        isActionActive(e);
    }
}
