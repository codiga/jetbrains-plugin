package com.code_inspector.plugins.intellij.actions;

import com.code_inspector.api.GetRecipesForClientQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;

public class AssistantAction extends AnAction {

    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);
    private JFrame jframe;
    private JTextField jTextField;

    class UpdateTextActionListener implements ActionListener{

        private AnActionEvent anActionEvent;

        public UpdateTextActionListener(AnActionEvent anActionEvent) {
            this.anActionEvent = anActionEvent;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
            Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
            Project project = anActionEvent.getDataContext().getData(LangDataKeys.PROJECT);
            LanguageEnumeration language = getLanguageFromFilename(virtualFile.getCanonicalPath());
            System.out.println(language);
            jframe.setVisible(false);
            String text = jTextField.getText();
            Document document = editor.getDocument();

            System.out.println(text);
            java.util.List<String> keywords = Arrays.<String>asList(text.split(" "));
            List<GetRecipesForClientQuery.GetRecipesForClient> recipes = codeInspectorApi.getRecipesForClient(keywords, ImmutableList.of(), Optional.empty(), language);

            if(!recipes.isEmpty()) {
                GetRecipesForClientQuery.GetRecipesForClient recipe = recipes.stream().findFirst().get();
                String code = new String(Base64.getDecoder().decode(recipe.code()));
                int offset = editor.getCaretModel().getOffset();

                try {
                    WriteCommandAction.writeCommandAction(project).run(
                            new ThrowableRunnable<Throwable>() {
                                @Override
                                public void run() throws Throwable {
                                    String finalCode = code.replaceAll("\r\n", "\n");
                                    document.insertString(offset, finalCode);
                                }
                            }
                    );
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            System.out.println(recipes);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        UpdateTextActionListener updateTextActionListener = new UpdateTextActionListener(event);
        jframe = new JFrame("Codiga");
        JPanel jPanel = new JPanel(new FlowLayout());
        jTextField = new JBTextField(20);
        JButton cancelButton = new JButton("cancel");
        jTextField.addActionListener(updateTextActionListener);
        cancelButton.addActionListener( a -> {
            jframe.setVisible(false);
        });

        jPanel.add(jTextField);
        jPanel.add(cancelButton);
        jframe.setUndecorated(true);
        jframe.add(jPanel);
        jframe.pack();
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
