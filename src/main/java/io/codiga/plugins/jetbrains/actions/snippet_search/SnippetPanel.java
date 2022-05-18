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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;

public class SnippetPanel {
    private JTextArea code;
    private JPanel mainPanel;
    private JButton insert;
    private JButton learnMore;
    private JLabel name;
    private JLabel userInformation;
    private JTextPane description;
    private CodeInsertionContext codeInsertionContext;
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);


    public SnippetPanel(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet, CodeInsertionContext _codeInsertionContext) {
        MarkdownDecorator markdownDecorator = new MarkdownDecorator();
        String htmlDescription = Processor.process(snippet.description(), markdownDecorator, true);
        codeInsertionContext = _codeInsertionContext;
        String owner = "unknown author";
        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes()));
        final String publicSnippetLink = String.format("https://app.codiga.io/hub/snippet/%s/view", snippet.id());
        final String privateSnippetLink = String.format("https://app.codiga.io/assistant/snippet/%s/view", snippet.id());


        learnMore.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    final String link = snippet.isPublic() ? publicSnippetLink : privateSnippetLink;
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
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
        });

        if (snippet.owner() != null){
            String userLink = String.format("https://app.codiga.io/hub/user/%s/%s", snippet.owner().accountType().toString().toLowerCase(), snippet.owner().username());
            owner = String.format("<html>Owner: <a href=\"%s\">%s</a></html>", userLink, snippet.owner().username());

            if (DesktopUtils.isBrowsingSupported()){
                userInformation.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI(userLink));
                        } catch (IOException | URISyntaxException e1) {
                            e1.printStackTrace();
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
                });
            }
        }
        LOGGER.info(owner);
        code.setText(decodedCode);


        userInformation.setText(owner);

        description.setText(String.format("<html>%s</html>", htmlDescription));

        name.setText(snippet.name());

        insert.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();
                FileEditor fileEditor = SnippetToolWindowFileEditorManagerListener.getCurrentFileEditor();
                VirtualFile virtualFile = SnippetToolWindowFileEditorManagerListener.getCurrentVirtualFile();

                if (project == null) {
                    return;
                }

                if (fileEditor == null || fileEditor.getFile() == null) {
                    return;
                }

                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                IdeFocusManager.getInstance(project).requestFocusInProject(editor.getComponent(), project);

                applyRecipe(editor,
                        project,
                        snippet.name(),
                        snippet.jetbrainsFormat(),
                        ((BigDecimal)snippet.id()).longValue(),
                        snippet.imports(),
                        snippet.language(),
                        codeInsertionContext,
                        codigaApi);
                insert.setText("Preview");
                codeInsertionContext.clearAll();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();
                FileEditor fileEditor = SnippetToolWindowFileEditorManagerListener.getCurrentFileEditor();
                VirtualFile virtualFile = SnippetToolWindowFileEditorManagerListener.getCurrentVirtualFile();

                if (project == null) {
                    return;
                }

                if (fileEditor == null || fileEditor.getFile() == null) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
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
                        new CodingAssistantContext(fileEditor.getFile(), project, psiFile));
                insert.setText("Insert");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();
                FileEditor fileEditor = SnippetToolWindowFileEditorManagerListener.getCurrentFileEditor();
                VirtualFile virtualFile = SnippetToolWindowFileEditorManagerListener.getCurrentVirtualFile();

                if (project == null) {
                    return;
                }
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                removeAddedCode(editor, project, codeInsertionContext);
                insert.setText("Preview");
            }
        });
    }

    public Component getComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
