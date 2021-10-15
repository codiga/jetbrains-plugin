package com.code_inspector.plugins.intellij.actions;

import com.code_inspector.api.GetRecipesForClientQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.google.common.collect.ImmutableList;
import com.intellij.icons.AllIcons;
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
import icons.CodigaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;

public class AssistantAction extends AnAction {

    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);
    private JFrame jframe;
    private JTextField jTextField;
    private JLabel jLabelResults = new JLabel("(enter search terms)");
    JButton nextButton = null;
    JButton previousButton = null;
    private boolean codeInserted = false;
    private int codeInsertedOffsetStart = 0;
    private int codeInsertedOffsetEnd = 0;
    private int currentRecipeIndex = 0;
    private long lastRequestTimestamp = 0;
    RangeHighlighter currentHighlighter = null;
    List<GetRecipesForClientQuery.GetRecipesForClient> currentRecipes = null;
    java.util.Timer timer = new java.util.Timer();

    public void removeAddedCode(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        Project project = anActionEvent.getDataContext().getData(LangDataKeys.PROJECT);

        Document document = editor.getDocument();

        if (document == null) {
            return;
        }

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


        String finalDescription = "no description";

        if (recipe.description().length() > 0){
            finalDescription = recipe.description();
        }

        jLabelResults.setText(String.format("result %s/%s: %s",
                currentRecipeIndex + 1, currentRecipes.size(), finalDescription));

        try {


            WriteCommandAction.writeCommandAction(project).run(
                    new ThrowableRunnable<Throwable>() {
                        @Override
                        public void run() throws Throwable {
                            int offset = editor.getCaretModel().getOffset();
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

    public void updateSuggestions(AnActionEvent anActionEvent){
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
        currentRecipeIndex = 0;
        updateButtonState();
        System.out.println(currentRecipes);
        if(currentRecipes.isEmpty()) {
            jLabelResults.setText("no result");
        } else {
            showCurrentRecipe(anActionEvent);

        }
    }



    private void applyRecipe(AnActionEvent anActionEvent){
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        currentRecipes = null;
        currentRecipeIndex = 0;
        codeInserted = false;
        codeInsertedOffsetStart = 0;
        codeInsertedOffsetEnd = 0;
        if (currentHighlighter != null) {
            editor.getMarkupModel().removeHighlighter(currentHighlighter);
        }
        currentHighlighter = null;

        jframe.setVisible(false);
    }

    private void cancelChanges(@NotNull AnActionEvent event) {
        if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
            removeAddedCode(event);
        }
        currentRecipes = null;
        currentRecipeIndex = 0;

        currentHighlighter = null;

        jframe.setVisible(false);
    }

    public void updateButtonState() {
        System.out.println(currentRecipeIndex);
        System.out.println(currentRecipes.size());

        if(previousButton != null && currentRecipeIndex == 0){
            previousButton.setEnabled(false);
            if (currentRecipeIndex < currentRecipes.size() - 1)
            {
                nextButton.setEnabled(true);
            }
            return;
        }
        if(nextButton != null && currentRecipeIndex == (currentRecipes.size() - 1)){
            if (currentRecipeIndex > 0)
            {
                previousButton.setEnabled(true);
            }
            nextButton.setEnabled(false);
            return;
        }
        nextButton.setEnabled(true);
        previousButton.setEnabled(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        jframe = new JFrame("Codiga Coding Assistant");
        //jframe.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

        // reset results
        jLabelResults.setText("(enter search terms)");
        currentRecipes = null;
        currentRecipeIndex = 0;
        codeInserted = false;
        codeInsertedOffsetEnd = 0;
        codeInsertedOffsetStart = 0;

        JPanel jPanelMain = new JPanel();
        jPanelMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jLabelResults.setBorder(BorderFactory.createEmptyBorder(0, CodigaIcons.Codiga_default_icon.getIconWidth() + 10, 0, 0));

        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        JPanel jPanelMiddle = new JPanel(new FlowLayout());
        JPanel jPanelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel codigaLabel = new JLabel(CodigaIcons.Codiga_default_icon);

        jPanelBottom.add(jLabelResults);

        jTextField = new JBTextField(40);
        JButton okButton = new JButton(AllIcons.Actions.Checked);
        JButton cancelButton = new JButton(AllIcons.Actions.Cancel);

        jTextField.addActionListener(l -> {
            applyRecipe(event);
        });
        Icon previousIcon = AllIcons.General.ArrowLeft;
        Icon nextIcon = AllIcons.General.ArrowRight;
        nextButton = new JButton(nextIcon);
        nextButton.setEnabled(false);

        nextButton.setMaximumSize(new Dimension(nextButton.getIcon().getIconWidth(),nextButton.getIcon().getIconHeight()));

        nextButton.setToolTipText("go to next suggestion");
        okButton.setToolTipText("apply the code recipe");
        previousButton = new JButton(previousIcon);
        previousButton.setToolTipText("go to previous suggestion");
        previousButton.setEnabled(false);
//        nextButton.setIcon(nextIcon);
//        previousButton.setIcon(previousIcon);

        /**
         * Closing the window
         */
        jframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelChanges(event);
            }
        });

        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelChanges(event);
                } else {
                    if (ke.getKeyCode() != KeyEvent.VK_ENTER) {
                        lastRequestTimestamp = System.currentTimeMillis();
                        long thisRequestTimestamp = lastRequestTimestamp;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if(thisRequestTimestamp == lastRequestTimestamp) {
                                    removeAddedCode(event);
                                    updateSuggestions(event);
                                }
                            }
                        }, 500);
                    }
                }
            }
        });
        okButton.addActionListener( a -> {
            applyRecipe(event);
        });
        cancelButton.addActionListener( a -> {
            cancelChanges(event);
        });
        nextButton.addActionListener( a -> {

            if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex + 1;
                showCurrentRecipe(event);
                updateButtonState();
            }
        });
        previousButton.addActionListener( a -> {
            if(currentRecipeIndex > 0) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex - 1;
                showCurrentRecipe(event);
                updateButtonState();
            }

        });

        jPanelMiddle.add(codigaLabel);
        jPanelMiddle.add(jTextField);
        jPanelMiddle.add(previousButton);
        jPanelMiddle.add(nextButton);
        jPanelMiddle.add(okButton);
        //jPanelMiddle.add(cancelButton);

        jPanelMain.add(jPanelMiddle);
        jPanelMain.add(jPanelBottom);
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
