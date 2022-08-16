package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.github.rjeschke.txtmark.Processor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.codiga.api.GetRecipesForClientSemanticQuery;
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

/**
 * This class renders only one snippet. This is created/instantiated
 * every time a snippet is rendered.
 */
public class SnippetPanel {
    private JPanel mainPanel;
    private JButton insert;
    private JButton learnMore;
    private JLabel name;
    private JLabel userInformation;
    private JTextPane description;
    private JLabel shortcutLabel;
    private JLabel visibilityLabel;

    private JButton copyToClipboard;
    private JTextArea code;

    private final CodeInsertionContext codeInsertionContext;
    private static final MarkdownDecorator markdownDecorator = new MarkdownDecorator();
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);


    public SnippetPanel(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet, CodeInsertionContext _codeInsertionContext) {
        codeInsertionContext = _codeInsertionContext;

        // The description is in Markdown, we need to decode it into HTML.
        String htmlDescription = Processor.process(snippet.description(), markdownDecorator, true);

        String owner = "unknown author"; // default value for the owner
        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes(StandardCharsets.UTF_8)));

        copyToClipboard.addMouseListener(new CopyToClipboardMouseListener(snippet));
        learnMore.addMouseListener(new LearnMoreMouseListener(snippet));

        if (snippet.owner() != null){

            if (snippet.owner().hasSlug() && snippet.owner().slug() != null) {
                owner = String.format("<html>Owner: <a>%s</a></html>", snippet.owner().displayName());

                if (DesktopUtils.isBrowsingSupported()){
                    userInformation.addMouseListener(new OwnerMouseListener(snippet.owner().slug()));
                }
            } else {
                owner = String.format("<html>Owner: %s</html>", snippet.owner().displayName());
            }

        }
        code.setText(decodedCode);

        userInformation.setText(owner);
        description.setText(String.format("<html>%s</html>", htmlDescription));
        name.setText(snippet.name());

        if (snippet.shortcut() == null) {
            shortcutLabel.setText("No Shortcut");
        } else {
            shortcutLabel.setText(snippet.shortcut());
        }

        if (snippet.isPublic()) {
            visibilityLabel.setText("public");
        } else {
            visibilityLabel.setText("private");
        }

        /**
         * Logic to preview or insert the snippet. When we hover the button, we preview
         * the snippet. Once clicked, we insert the snippet into the code.
         */
        insert.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();

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

                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();
                VirtualFile virtualFile = SnippetToolWindowFileEditorManagerListener.getCurrentVirtualFile();

                if (project == null) {
                    LOGGER.info("[mouseEntered] project is null");
                    return;
                }

                if (virtualFile == null) {
                    LOGGER.info("[mouseEntered] virtualFile is null");
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                if (editor == null) {
                    return;
                }

                removeAddedCode(editor, project, codeInsertionContext);
                addRecipeToEditor(
                        editor,
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
                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();

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


}
