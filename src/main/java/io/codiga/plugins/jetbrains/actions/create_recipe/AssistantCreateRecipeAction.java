package io.codiga.plugins.jetbrains.actions.create_recipe;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        // Get the file and editor that is being active.
        VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        // If no file or editor, just return
        if (virtualFile == null || editor == null) {
            return;
        }

        // Get the selected text, the language and encode the text to be used as a recipe.
        String content = editor.getSelectionModel().getSelectedText();

        /**
         * If content is null, we show a dialog that invites to selext text.
         */
        if (content == null) {
            DialogWrapper dialogWrapper = new DialogWrapper(editor.getProject(), false) {
                @Override
                protected @Nullable JComponent createCenterPanel() {
                    return null;
                }
            };
            JBLabel jbLabel = new JBLabel("Please select text to create recipe");
            jbLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
            dialogWrapper.setResizable(false);
            JButton jButton = new JButton("OK");
            dialogWrapper.getContentPane().setLayout(new BorderLayout());
            dialogWrapper.getContentPane().add(jbLabel, BorderLayout.CENTER);
            dialogWrapper.getContentPane().add(jButton, BorderLayout.SOUTH);
            jButton.addActionListener(e -> dialogWrapper.close(0));
            dialogWrapper.setTitle("Codiga Recipe Creation");

            dialogWrapper.setOKActionEnabled(true);
            dialogWrapper.pack();

            dialogWrapper.show();
            return;
        }

        // We need to replace the '+' sign by %2B, otherwise, the code encoding
        // is not correct in the URL parameters.
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)).replaceAll("\\+", "%2B");
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

        // URL to go once the user clicked.
        String urlString = String.format("%s/assistant/recipe/create?code=%s&language=%s", FRONTEND_URL, encodedContent, language.rawValue());
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (IOException | URISyntaxException e) {
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
