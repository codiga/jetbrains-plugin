package io.codiga.plugins.jetbrains.actions;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.macro.EscapeStringMacro;
import com.intellij.formatting.Indent;
import io.codiga.api.type.LanguageEnumeration;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.codiga.plugins.jetbrains.Constants.FRONTEND_URL;

/**
 * Action to create a recipe. This action appears in the contextual menu
 * when selecting code.
 */
public class AssistantCreateRecipeAction extends AnAction {
    public AssistantCreateRecipeAction() {
        super();
    }


    /**
     * This constructor is used to support dynamically added menu actions.
     * It sets the text, description to be displayed for the menu item.
     * Otherwise, the default AnAction constructor is used by the IntelliJ Platform.
     *
     * @param text        The text to be displayed as a menu item.
     * @param description The description of the menu item.
     * @param icon        The icon to be used with the menu item.
     */
    public AssistantCreateRecipeAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    /**
     * Action that is invoked once the user selected the text.
     *
     * @param anActionEvent Event received when the associated menu item is chosen.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        /**
         * Get the file and editor that is being active.
         */
        VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        // If no file or editor, just return
        if (virtualFile == null || editor == null){
            return;
        }

        // Get the selected text, the language and encode the text to be used as a recipe.
        String content = editor.getSelectionModel().getSelectedText();
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

        // URL to go once the user clicked.
        String urlString = String.format("%s/assistant/recipe/create?code=%s&language=%s", FRONTEND_URL, encodedContent, language.rawValue());
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines whether this menu item is available for the current context.
     * Requires a project to be open.
     *
     * @param e Event received when the associated group-id menu is chosen.
     */
    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
