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
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
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
    private JLabel jLabelResults = new JLabel("(enter search terms)");
    private JLabel jLabelDescription = new JLabel(" ");
    private boolean codeInserted = false;
    private int codeInsertedOffsetStart = 0;
    private int codeInsertedOffsetEnd = 0;
    private int currentRecipeIndex = 0;
    RangeHighlighter currentHighlighter = null;
    List<GetRecipesForClientQuery.GetRecipesForClient> currentRecipes = null;

    public void removeAddedCode(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        Project project = anActionEvent.getDataContext().getData(LangDataKeys.PROJECT);

        Document document = editor.getDocument();
        if(codeInserted) {
            try{
                WriteCommandAction.writeCommandAction(project).run(
                        new ThrowableRunnable<Throwable>() {
                            @Override
                            public void run() throws Throwable {
                                document.deleteString(codeInsertedOffsetStart, codeInsertedOffsetEnd);
                                codeInserted = false;
                            }
                        }
                );
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    public void showCurrentRecipe(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        Project project = anActionEvent.getDataContext().getData(LangDataKeys.PROJECT);
        Document document = editor.getDocument();

        GetRecipesForClientQuery.GetRecipesForClient recipe = currentRecipes.get(currentRecipeIndex);
        String code = new String(Base64.getDecoder().decode(recipe.code()));
        int offset = editor.getCaretModel().getOffset();
        jLabelResults.setText(String.format("result %s out of %s", currentRecipeIndex + 1, currentRecipes.size()));
        jLabelDescription.setText(recipe.description());

        try {
            WriteCommandAction.writeCommandAction(project).run(
                    new ThrowableRunnable<Throwable>() {
                        @Override
                        public void run() throws Throwable {
                            String finalCode = code.replaceAll("\r\n", "\n");
                            int startOffset = offset;
                            int endOffset = startOffset + finalCode.length();
                            codeInsertedOffsetStart = startOffset;
                            codeInsertedOffsetEnd = endOffset;
                            codeInserted = true;
                            document.insertString(offset, finalCode);

                            currentHighlighter = editor.getMarkupModel()
                                    .addRangeHighlighter(startOffset, endOffset, 0,
                                            new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.PINK, EffectType.ROUNDED_BOX, 13),
                                            HighlighterTargetArea.EXACT_RANGE);
                        }
                    }
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    class UpdateTextActionListener implements ActionListener{

        private AnActionEvent anActionEvent;

        public UpdateTextActionListener(AnActionEvent anActionEvent) {
            this.anActionEvent = anActionEvent;
        }



        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // Get the language to prepare the request
            VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);

            if (virtualFile == null){
                return;
            }

            LanguageEnumeration language = getLanguageFromFilename(virtualFile.getCanonicalPath());

            // get the keywords
            String text = jTextField.getText();
            System.out.println("keywords");
            System.out.println(text);
            java.util.List<String> keywords = Arrays.<String>asList(text.split(" "));
            currentRecipes = codeInspectorApi.getRecipesForClient(keywords, ImmutableList.of(), Optional.empty(), language);
            System.out.println(currentRecipes);
            if(currentRecipes.isEmpty()) {
                jLabelResults.setText("no result");
            } else {
                showCurrentRecipe(anActionEvent);
            }
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        UpdateTextActionListener updateTextActionListener = new UpdateTextActionListener(event);
        jframe = new JFrame("Codiga");

        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        JPanel jPanelMiddle = new JPanel(new FlowLayout());
        JPanel jPanelBottom = new JPanel(new FlowLayout());
        JPanel jPanelBottomDescription = new JPanel(new FlowLayout());
        JPanel jPannelUp = new JPanel(new FlowLayout());
        jPannelUp.add(new JBLabel("Codiga Coding Assistant"));
        jPanelBottom.add(jLabelResults);
        jPanelBottomDescription.add(jLabelDescription);

        jTextField = new JBTextField(40);
        JButton okButton = new JButton("ok");
        JButton cancelButton = new JButton("cancel");
        JButton nextButton = new JButton("next");
        JButton previousButton = new JButton("previous");
        jTextField.addActionListener(updateTextActionListener);
        okButton.addActionListener( a -> {
            currentRecipes = null;
            currentRecipeIndex = 0;
            if (currentHighlighter != null) {
                editor.getMarkupModel().removeHighlighter(currentHighlighter);
            }
            currentHighlighter = null;

            jframe.setVisible(false);
        });
        cancelButton.addActionListener( a -> {
            if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
                removeAddedCode(event);
            }
            currentRecipes = null;
            currentRecipeIndex = 0;

            currentHighlighter = null;

            jframe.setVisible(false);
        });
        nextButton.addActionListener( a -> {

            if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex + 1;
                showCurrentRecipe(event);
            }
        });
        previousButton.addActionListener( a -> {
            if(currentRecipeIndex > 0) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex - 1;
                showCurrentRecipe(event);
            }

        });

        jPanelMiddle.add(jTextField);
        jPanelMiddle.add(previousButton);
        jPanelMiddle.add(nextButton);
        jPanelMiddle.add(okButton);
        jPanelMiddle.add(cancelButton);

        jPanelMain.add(jPannelUp);
        jPanelMain.add(jPanelMiddle);
        jPanelMain.add(jPanelBottom);
        jPanelMain.add(jPanelBottomDescription);
//        jframe.setUndecorated(true);
        jframe.add(jPanelMain);
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
