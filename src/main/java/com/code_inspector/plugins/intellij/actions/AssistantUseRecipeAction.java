package com.code_inspector.plugins.intellij.actions;

import com.code_inspector.api.GetRecipesForClientQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.dependencies.DependencyManagement;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.model.CodeInsertion;
import com.google.common.collect.ImmutableList;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ThrowableRunnable;
import icons.CodigaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.code_inspector.plugins.intellij.Constants.LINE_SEPARATOR;
import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;
import static com.code_inspector.plugins.intellij.utils.CodeImportUtils.generateImportStatement;
import static com.code_inspector.plugins.intellij.utils.CodeImportUtils.hasDependency;
import static com.code_inspector.plugins.intellij.utils.CodePositionUtils.*;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantUseRecipeAction extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public static final String ENTER_SEARCH_TERM_TEXT = "(enter search terms)";

    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);

    // UI elements
    private JTextField searchTextfield;
    private WindowWrapper windowWrapper; // Window Wrapper that looks like IntelliJ
    private JLabel jLabelResults = new JLabel(ENTER_SEARCH_TERM_TEXT);
    private JButton nextButton = null;
    private JButton previousButton = null;
    private JButton okButton = null;

    // status of the action: is code inserted, what are the recipes, etc.
    private boolean codeInserted = false;
    private final List<CodeInsertion> codeInsertions = new ArrayList<CodeInsertion>();

    private int currentRecipeIndex = 0;
    private long lastRequestTimestamp = 0;
    private final List<RangeHighlighter> highlighters = new ArrayList<>();

    List<GetRecipesForClientQuery.GetRecipesForClient> currentRecipes = null;

    DependencyManagement dependencyManagement = new DependencyManagement();

    // used to get the list of recipes, trigger the recipes 500 ms after
    // the user finished typing.
    java.util.Timer timer = new java.util.Timer();

    /**
     * Remove the code that was previously added when browsing a recipe.
     * Remove the added code from the editor.
     * @param anActionEvent
     */
    public void removeAddedCode(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            return;
        }
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();

        if (document == null || project == null) {
            LOGGER.info("showCurrentRecipe - editor, project or document is null");
            return;
        }

        if(codeInserted) {
            try{
                WriteCommandAction.writeCommandAction(project).run(
                        (ThrowableRunnable<Throwable>) () -> {
                            int deletedLength = 0;
                            for(CodeInsertion codeInsertion: codeInsertions) {
                                document.deleteString(codeInsertion.getPositionStart() - deletedLength, codeInsertion.getPositionEnd() - deletedLength);
                                deletedLength = deletedLength + (codeInsertion.getPositionEnd() - codeInsertion.getPositionStart());
                            }

                            codeInsertions.clear();
                            codeInserted = false;
                        }
                );
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show the current recipe. The current recipe is stored in
     * the currentRecipeIndex and all the recipes in currentRecipes.
     * @param anActionEvent
     */
    public void showCurrentRecipe(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();
        String currentCode = document.getText();

        if (editor == null || project == null || document == null) {
            LOGGER.info("showCurrentRecipe - editor, project or document is null");
            return;
        }

        // make sure there are enough elements in the array.
        if (currentRecipeIndex >= currentRecipes.size()) {
            LOGGER.warn("showCurrentRecipe - incorrect recipe index");
            return;
        }

        GetRecipesForClientQuery.GetRecipesForClient recipe = currentRecipes.get(currentRecipeIndex);

        // Get the code from the recipe and remove all \r\n which are not use by IntelliJ
        String code = new String(Base64.getDecoder().decode(recipe.code())).replaceAll("\r\n", LINE_SEPARATOR);

        // Get the current line and get the indentation
        int selectedLine = editor.getCaretModel().getVisualPosition().getLine();
        String currentLine = document.getText(new TextRange(document.getLineStartOffset(selectedLine), document.getLineEndOffset(selectedLine)));
        int indentationCurrentLine = getIndentation(currentLine);

        // reindent the code based on the indentation of the current line.
        String indentedCode = indentOtherLines(code, indentationCurrentLine);

        // Update the label in the box with the description.
        String finalDescription = recipe.description().length() == 0 ? "no description" : recipe.description();
        String descriptionLabelText = String.format("result %s/%s: %s", currentRecipeIndex + 1, currentRecipes.size(), finalDescription);
        jLabelResults.setText(descriptionLabelText);

        // add the code and update global variables to indicate code has been inserted.
        try {
            WriteCommandAction.writeCommandAction(project).run(
                    (ThrowableRunnable<Throwable>) () -> {
                        List<String> imports = recipe.imports();
                        int editorOffset = editor.getCaretModel().getOffset();
                        int firstInsertion = firstPositionToInsert(currentCode, recipe.language());
                        int lengthInsertedForImport = 0;

                        for(String importName: imports) {
                            if(!hasDependency(currentCode, importName, recipe.language())) {
                                Optional<String> dependencyStatementOptional = generateImportStatement(importName, recipe.language());

                                if (!dependencyStatementOptional.isPresent()) {
                                    continue;
                                }

                                String dependencyStatement = dependencyStatementOptional.get() + LINE_SEPARATOR;

                                codeInsertions.add(new CodeInsertion(
                                        dependencyStatement,
                                        firstInsertion + lengthInsertedForImport,
                                        firstInsertion + lengthInsertedForImport + dependencyStatement.length()));
                                lengthInsertedForImport = lengthInsertedForImport + dependencyStatement.length();
                            }
                        }

                        int startOffset = editorOffset + lengthInsertedForImport;
                        int endOffset = startOffset + indentedCode.length();
                        codeInsertions.add(new CodeInsertion(indentedCode, startOffset, endOffset));

                        codeInserted = true;

                        for (CodeInsertion codeInsertion: codeInsertions) {
                            document.insertString(codeInsertion.getPositionStart(), codeInsertion.getCode());

                            RangeHighlighter newHighlighter = editor.getMarkupModel()
                                    .addRangeHighlighter(codeInsertion.getPositionStart(), codeInsertion.getPositionStart() + codeInsertion.getCode().length(), 0,
                                            new TextAttributes(JBColor.black, JBColor.WHITE, JBColor.PINK, EffectType.ROUNDED_BOX, 13),
                                            HighlighterTargetArea.EXACT_RANGE);
                            highlighters.add(newHighlighter);
                        }
                    }
            );
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("showCurrentRecipe - impossible to update the code from the recipe");
            LOGGER.error(e);
        }
    }

    public void updateSuggestions(AnActionEvent anActionEvent){
        // Get the language to prepare the request
        VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);

        if (virtualFile == null){
            LOGGER.error("updateSuggestions - cannot get virtualFile");
            return;
        }

        LanguageEnumeration language = getLanguageFromFilename(virtualFile.getCanonicalPath());

        // get the keywords and get them as a list.
        String text = searchTextfield.getText();
        java.util.List<String> keywords = Arrays.<String>asList(text.split(" "));

        // if there is no keywords, just reset.
        if(keywords.isEmpty() || searchTextfield.getText().length() == 0) {
            removeAddedCode(anActionEvent);
            currentRecipes = null;
            currentRecipeIndex = 0;
            jLabelResults.setText(ENTER_SEARCH_TERM_TEXT);
            highlighters.clear();
            updateButtonState();
            return;
        }

        // get the list of keywords from the API
        try{
            List<String> dependenciesName = dependencyManagement.getDependencies(psiFile).stream().map(d -> d.getName()).collect(Collectors.toList());
            currentRecipes = codeInspectorApi.getRecipesForClient(
                    keywords,
                    dependenciesName,
                    Optional.empty(),
                    language);
            currentRecipeIndex = 0;
            updateButtonState();

            if(currentRecipes.isEmpty()) {
                LOGGER.info("updateSuggestions - no suggestion");
                jLabelResults.setText("no result");
            } else {
                showCurrentRecipe(anActionEvent);
            }
        } catch (Exception e) {
            LOGGER.error("updateSuggestions - fail to get suggestions.");
            LOGGER.error(e);
        }
    }

    /**
     * Apply the recipe. We send a callback to the API and
     * remove all highlighted code in the editor.
     *
     * @param anActionEvent
     */
    private void applyRecipe(AnActionEvent anActionEvent){
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        if (editor == null) {
            LOGGER.warn("applyRecipe - editor is null");
            return;
        }

        /**
         * Record the use of the recipe.
         */
        if (currentRecipes != null && currentRecipeIndex <= currentRecipes.size()) {
            GetRecipesForClientQuery.GetRecipesForClient insertedRecipe = currentRecipes.get(currentRecipeIndex);
            if (insertedRecipe != null)
            {
                long recipeId = ((BigDecimal) insertedRecipe.id()).longValue();
                codeInspectorApi.recordRecipeUse(recipeId);
            } else {
                LOGGER.warn("applyRecipe - inserted recipe is null");
            }
        }

        // reset global variables so that current recipes are not kept in memory.
        currentRecipes = null;
        currentRecipeIndex = 0;
        codeInserted = false;
        codeInsertions.clear();

        // remmove the highlighted code.
        for (RangeHighlighter rangeHighlighter: highlighters) {
            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        }
        highlighters.clear();

        windowWrapper.close();
    }

    /**
     * Cancel any change done in the editor.
     * @param event
     */
    private void cancelChanges(@NotNull AnActionEvent event) {
        Editor editor = event.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        // remmove the highlighted code.
        for (RangeHighlighter rangeHighlighter: highlighters) {
            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        }
        highlighters.clear();

        if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
            removeAddedCode(event);
        }
        currentRecipes = null;
        currentRecipeIndex = 0;
        highlighters.clear();
    }

    /**
     * Update the status of the next and previous button by
     * making them active or inactive based on the recipe index.
     */
    public void updateButtonState() {
        if(currentRecipes == null || currentRecipes.isEmpty()) {
            nextButton.setEnabled(false);
            previousButton.setEnabled(false);
            okButton.setEnabled(false);
            return;
        }

        // We have recipes so we can enable the ok button
        okButton.setEnabled(true);

        // detect if we are at the beginning of the recipe selection.
        if(previousButton != null && currentRecipeIndex == 0){
            previousButton.setEnabled(false);
            if (currentRecipeIndex < currentRecipes.size() - 1)
            {
                nextButton.setEnabled(true);
            }
            return;
        }

        // detect if we are at the end of the recipe selection.
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

    /**
     * Main class that is triggered by the action. It creates the
     * window and everything we need to handle recipes listing.
     *
     * @param event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // reset results
        jLabelResults.setText(ENTER_SEARCH_TERM_TEXT);
        currentRecipes = null;
        currentRecipeIndex = 0;
        codeInserted = false;
        codeInsertions.clear();

        // main panel.
        JPanel jPanelMain = new JPanel();
        jLabelResults.setBorder(BorderFactory.createEmptyBorder(0, CodigaIcons.Codiga_default_icon.getIconWidth() + 10, 0, 0));

        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));

        JPanel jPanelMiddle = new JPanel(new FlowLayout());
        JPanel jPanelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // bottom panel
        jPanelBottom.add(jLabelResults);

        // middle panel
        JLabel codigaLabel = new JLabel(CodigaIcons.Codiga_default_icon);
        codigaLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        searchTextfield = new JBTextField(40);

        /**
         * Listener to apply the recipe when pressing enter.
         */
        searchTextfield.addActionListener(l -> {
            applyRecipe(event);
        });

        searchTextfield.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lastRequestTimestamp = System.currentTimeMillis();
                long thisRequestTimestamp = lastRequestTimestamp;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // make sure the latest request is the one updating the UI
                        if(thisRequestTimestamp == lastRequestTimestamp) {
                            removeAddedCode(event);
                            updateSuggestions(event);
                        }
                    }
                }, 500);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lastRequestTimestamp = System.currentTimeMillis();
                long thisRequestTimestamp = lastRequestTimestamp;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // make sure the latest request is the one updating the UI
                        if(thisRequestTimestamp == lastRequestTimestamp) {
                            removeAddedCode(event);
                            updateSuggestions(event);
                        }
                    }
                }, 500);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // nothing
            }
        });


        searchTextfield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelChanges(event);
                    windowWrapper.close();
                }
            }
        });

        okButton = new JButton(AllIcons.Actions.Checked);
        okButton.setEnabled(false);
        okButton.setToolTipText("apply the code recipe");
        okButton.addActionListener( a -> {
            applyRecipe(event);
        });

        nextButton = new JButton(AllIcons.General.ArrowRight);
        nextButton.setEnabled(false);
        nextButton.setMaximumSize(new Dimension(nextButton.getIcon().getIconWidth(),nextButton.getIcon().getIconHeight()));
        nextButton.setToolTipText("go to next suggestion");
        nextButton.addActionListener( a -> {
            if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex + 1;
                showCurrentRecipe(event);
                updateButtonState();
            }
        });

        previousButton = new JButton(AllIcons.General.ArrowLeft);
        previousButton.setToolTipText("go to previous suggestion");
        previousButton.setEnabled(false);
        previousButton.addActionListener( a -> {
            if(currentRecipeIndex > 0) {
                removeAddedCode(event);
                currentRecipeIndex = currentRecipeIndex - 1;
                showCurrentRecipe(event);
                updateButtonState();
            }
        });

        jPanelMiddle.add(codigaLabel);
        jPanelMiddle.add(searchTextfield);
        jPanelMiddle.add(previousButton);
        jPanelMiddle.add(nextButton);
        jPanelMiddle.add(okButton);

        // build the main panel
        jPanelMain.add(jPanelMiddle);
        jPanelMain.add(jPanelBottom);

        // Build the main window to keep it with an IntelliJ style
        windowWrapper = new WindowWrapperBuilder(WindowWrapper.Mode.FRAME, jPanelMain)
                .setProject(event.getDataContext().getData(LangDataKeys.PROJECT))
                .setPreferredFocusedComponent(searchTextfield)
                .setTitle("Codiga Coding Assistant")
                .setDimensionServiceKey("Codiga.Assistant") // key to remember the dimension on the local client
                .setPreferredFocusedComponent(searchTextfield)
                .setOnCloseHandler(() -> {
                    cancelChanges(event);
                    return true;
                })
                .build();
        windowWrapper.getWindow().setPreferredSize(new Dimension(800, 100));
        windowWrapper.getWindow().setSize(new Dimension(800, 100));

        windowWrapper.show();

        // Force repaint after shown to force the dimensions
        // This is a hack and should we improved
        windowWrapper.getWindow().repaint();
        windowWrapper.getWindow().pack();
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
