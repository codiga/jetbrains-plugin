package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.github.rjeschke.txtmark.Processor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorTextField;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.CodeInsertionContext;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.utils.DesktopUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;
import static io.codiga.plugins.jetbrains.utils.LanguageUtils.getFileTypeForLanguage;

/**
 * This class renders only one snippet. This is created/instantiated every time a snippet is rendered.
 * <p>
 * <h3>Notes on the code snippet text field creation</h3>
 * The code snippet {@link EditorTextField} is created manually due to a few reasons related to how forms
 * created by the UI designer operate.
 * <p>
 * The {@code EditorTextField} instance requires a non-null Project instance in order to do syntax highlighting, but
 * <ul>
 *     <li>having it instantiated by the "UI designer" would pass in a null Project</li>
 *     <li>{@code createUIComponents()} is executed before the constructor, so we cannot have an actual
 *     Project instance there either.</li>
 * </ul>
 */
public class SnippetPanel {
    private static final Color CODE_SNIPPET_BACKGROUND_COLOR = new Color(69, 73, 74);

    private JPanel mainPanel;
    private JButton insert;
    private JButton learnMore;
    private JLabel name;
    private JLabel userInformation;
    private JTextPane description;
    private JLabel shortcutLabel;
    private JLabel visibilityLabel;

    private JButton copyToClipboard;
    private JPanel codePanel;

    private final CodeInsertionContext codeInsertionContext;
    private static final MarkdownDecorator markdownDecorator = new MarkdownDecorator();
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final ToolWindow toolWindow;

    public SnippetPanel(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet,
                        CodeInsertionContext _codeInsertionContext,
                        ToolWindow toolWindow,
                        Project project) {
        codeInsertionContext = _codeInsertionContext;
        this.toolWindow = toolWindow;

        // The description is in Markdown, we need to decode it into HTML.
        String htmlDescription = Processor.process(snippet.description(), markdownDecorator, true);

        String owner = "unknown author"; // default value for the owner

        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes(StandardCharsets.UTF_8)));
        //BorderLayout helps with aligning the text field to the left and making it fill its parent horizontally
        codePanel.add(createCodeSnippetField(snippet.language(), project, decodedCode), BorderLayout.PAGE_START);

        copyToClipboard.addMouseListener(new CopyToClipboardMouseListener(snippet));
        learnMore.addMouseListener(new LearnMoreMouseListener(snippet));

        if (snippet.owner() != null) {

            if (snippet.owner().hasSlug() && snippet.owner().slug() != null) {
                owner = String.format("<html>Owner: <a>%s</a></html>", snippet.owner().displayName());

                if (DesktopUtils.isBrowsingSupported()) {
                    userInformation.addMouseListener(new OwnerMouseListener(snippet.owner().slug()));
                }
            } else {
                owner = String.format("<html>Owner: %s</html>", snippet.owner().displayName());
            }

        }

        userInformation.setText(owner);
        description.setText(String.format("<html>%s</html>", htmlDescription));
        name.setText(snippet.name());

        if (snippet.shortcut() == null) {
            shortcutLabel.setText("No Shortcut");
        } else {
            shortcutLabel.setText(snippet.shortcut());
        }

        if (snippet.isPublic()) {
            visibilityLabel.setText("Visibility: public");
        } else {
            visibilityLabel.setText("Visibility: private");
        }

        /**
         * Logic to preview or insert the snippet. When we hover the button, we preview
         * the snippet. Once clicked, we insert the snippet into the code.
         */
        insert.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (project == null) {
                    LOGGER.info("[mouseClicked] project is null");
                    return;
                }

                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                if (editor == null) {
                    LOGGER.info("[mouseClicked] editor is null");
                    return;
                }

                IdeFocusManager.getInstance(project).requestFocusInProject(editor.getContentComponent(), project);

                applyRecipe(editor,
                        project,
                        snippet.name(),
                        snippet.jetbrainsFormat(),
                        ((BigDecimal)snippet.id()).longValue(),
                        snippet.imports(),
                        snippet.language(),
                        codeInsertionContext,
                        codigaApi);

                IdeFocusManager.getInstance(project).requestFocusInProject(editor.getContentComponent(), project);

                insert.setText("Preview");
                codeInsertionContext.clearAll();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // empty, nothing needed here
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // empty, nothing needed here
            }

            @Override
            public void mouseEntered(MouseEvent e) {

                if (project == null) {
                    LOGGER.info("[mouseEntered] project is null");
                    return;
                }
                FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor();
                Editor textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                if (editor == null || textEditor == null) {
                    LOGGER.info("[mouseEntered] editor is null");
                    return;
                }

                VirtualFile virtualFile = editor.getFile();

                if (virtualFile == null) {
                    LOGGER.info("[mouseEntered] virtualFile is null");
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);


                removeAddedCode(textEditor, project, codeInsertionContext);
                addRecipeToEditor(
                        textEditor,
                        psiFile,
                        project,
                        codeInsertionContext.getCodeInsertions(),
                        codeInsertionContext.getHighlighters(),
                        snippet.imports(),
                        snippet.jetbrainsFormat(),
                        snippet.language(),
                        new CodingAssistantContext(virtualFile, project, psiFile));
                insert.setText("Insert");
            }

            @Override
            public void mouseExited(MouseEvent e) {

                if (project == null) {
                    return;
                }
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                if (editor == null) {
                    return;
                }

                removeAddedCode(editor, project, codeInsertionContext);
                insert.setText("Preview");
            }
        });
    }

    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Creates the code snippet text field configured with the language for which to do syntax highlighting,
     * and with the text of the code to display.
     *
     * @param language the language to do syntax highlighting for
     * @param project the current project
     * @param decodedCode the code to display in the text field
     */
    public EditorTextField createCodeSnippetField(LanguageEnumeration language,
                                                 Project project,
                                                 String decodedCode) {
        var code = new EditorTextField(decodedCode, project, getFileTypeForLanguage(language));
        code.setViewer(true); //the field is read-only
        code.setOneLineMode(false); //the field is multiline
        code.setFont(code.getFont().deriveFont(13f));
        code.setFontInheritedFromLAF(false); // use font as in regular editor
        code.setBackground(CODE_SNIPPET_BACKGROUND_COLOR);
        //Soft wrap works properly when the width of the component is restricted
        code.setPreferredWidth(400);
        code.addSettingsProvider(editor -> editor.getSettings().setUseSoftWraps(true));
        return code;
    }
}
