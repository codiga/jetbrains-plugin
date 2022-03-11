package io.codiga.plugins.jetbrains.actions;

import com.github.rjeschke.txtmark.Processor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodeInsertion;
import io.codiga.plugins.jetbrains.model.CodingAssistantCodigaTransform;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

import static io.codiga.plugins.jetbrains.Constants.LINE_SEPARATOR;
import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;
import static io.codiga.plugins.jetbrains.utils.CodePositionUtils.*;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantUseRecipeAction extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public static final String ENTER_SEARCH_TERM_TEXT = "(enter search terms)";
    private final CodigaMarkdownDecorator codigaMarkdownDecorator = new CodigaMarkdownDecorator();
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    // UI elements
    private JTextField searchTextfield;
    private WindowWrapper windowWrapper; // Window Wrapper that looks like IntelliJ
    private final JLabel jLabelResults = new JLabel(ENTER_SEARCH_TERM_TEXT);
    private JButton nextButton = null;
    private JButton previousButton = null;
    private JButton okButton = null;
    private final JEditorPane jEditorPane = new JEditorPane();
    private final JBScrollPane scrollPane = new JBScrollPane(jEditorPane);

    // status of the action: is code inserted, what are the recipes, etc.
    private boolean codeInserted = false;
    private final List<CodeInsertion> codeInsertions = new ArrayList<CodeInsertion>();

    private int currentRecipeIndex = 0;
    private long lastRequestTimestamp = 0;
    private final List<RangeHighlighter> highlighters = new ArrayList<>();

    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> currentRecipes = null;

    // used to get the list of recipes, trigger the recipes 500 ms after
    // the user finished typing.
    java.util.Timer timer = new java.util.Timer();


    /**
     * Show the current recipe. The current recipe is stored in
     * the currentRecipeIndex and all the recipes in currentRecipes.
     * @param anActionEvent
     */
    public void showCurrentRecipe(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (Objects.isNull(editor)) {
            LOGGER.info("showCurrentRecipe - editor is null");
            return;
        }
        Project project = anActionEvent.getProject();
        Document document = editor.getDocument();
        if (Objects.isNull(project) || Objects.isNull(document)) {
            LOGGER.info("showCurrentRecipe - project or document is null");
            return;
        }
        // make sure there are enough elements in the array.
        if (currentRecipeIndex >= currentRecipes.size()) {
            LOGGER.warn("showCurrentRecipe - incorrect recipe index");
            return;
        }

        GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch recipe = currentRecipes.get(currentRecipeIndex);

        // Get the code from the recipe and remove all \r\n which are not use by IntelliJ
        String unprocessedCode = new String(Base64.getDecoder().decode(recipe.jetbrainsFormat())).replaceAll("\r\n", LINE_SEPARATOR);
        // process supported variables dynamically
        final CodingAssistantContext CodigaTransformationContext = new CodingAssistantContext(anActionEvent.getDataContext());
        final CodingAssistantCodigaTransform codingAssistantCodigaTransform = new CodingAssistantCodigaTransform(CodigaTransformationContext);
        String code = codingAssistantCodigaTransform.findAndTransformVariables(unprocessedCode);

        // Get the current line and get the indentation
        int selectedLine = editor.getCaretModel().getVisualPosition().getLine();
        String currentLine = document.getText(new TextRange(document.getLineStartOffset(selectedLine), document.getLineEndOffset(selectedLine)));
        final boolean usesTabs = detectIfTabs(currentLine);
        int indentationCurrentLine = getIndentation(currentLine, usesTabs);

        // reindent the code based on the indentation of the current line.
        String finalDescription = recipe.description().length() == 0 ? "no description" : recipe.description();
        String shortcutText = (recipe.shortcut() == null || recipe.shortcut().length() == 0) ? "no shortcut" : "`"+recipe.shortcut()+"`";
        String finalDescriptionWithLink = finalDescription +
                String.format("\n\nShortcut: %s\n\n", shortcutText) +
                String.format("\n\n[%s](https://app.codiga.io/hub/recipe/%s/view)", "View Recipe on Codiga", recipe.id());


        String html = Processor.process(finalDescriptionWithLink, codigaMarkdownDecorator);
        jEditorPane.setContentType("text/html");
        jEditorPane.setText(html);

        // Update the label in the box with the description.
        String descriptionLabelText = String.format("result %s/%s: %s", currentRecipeIndex + 1, currentRecipes.size(), recipe.name());
        jLabelResults.setText(descriptionLabelText);

        addRecipeToEditor(anActionEvent, codeInsertions, highlighters, recipe.imports(), recipe.jetbrainsFormat(), recipe.language());
    }

    public void updateSuggestions(AnActionEvent anActionEvent){
        // Get the language to prepare the request
        VirtualFile virtualFile = anActionEvent.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);

        if (virtualFile == null) {
            LOGGER.error("updateSuggestions - cannot get virtualFile");
            return;
        }

        LanguageEnumeration language = getLanguageFromEditorForEvent(anActionEvent);

        // get the keywords and get them as a list.
        String text = searchTextfield.getText();

        // if there is no keywords, just reset.
        if (text.isEmpty()) {
            removeAddedCode(anActionEvent, codeInsertions, highlighters);
            currentRecipes = null;
            currentRecipeIndex = 0;
            jLabelResults.setText(ENTER_SEARCH_TERM_TEXT);
            highlighters.clear();
            updateButtonState();
            return;
        }

        // get the list of keywords from the API
        try{
            List<String> dependenciesName = getDependenciesFromEditorForEvent(anActionEvent);
            String filename = getFilenameFromEditorForEvent(anActionEvent);

            currentRecipes = codigaApi.getRecipesSemantic(
                    Optional.ofNullable(text),
                    dependenciesName,
                    Optional.empty(),
                    language,
                    filename);

            currentRecipeIndex = 0;
            updateButtonState();

            if (currentRecipes.isEmpty()) {
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
     * Cancel any change done in the editor.
     * @param event
     */
    private void cancelChanges(@NotNull AnActionEvent event) {
        Editor editor = event.getDataContext().getData(LangDataKeys.EDITOR_EVEN_IF_INACTIVE);

        // remove the highlighted code.
        for (RangeHighlighter rangeHighlighter: highlighters) {
            editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        }
        highlighters.clear();

        if (currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
            removeAddedCode(event, codeInsertions, highlighters);
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
        if (currentRecipes == null || currentRecipes.isEmpty()) {
            nextButton.setEnabled(false);
            previousButton.setEnabled(false);
            okButton.setEnabled(false);
            return;
        }

        // We have recipes so we can enable the ok button
        okButton.setEnabled(true);

        // detect if we are at the beginning of the recipe selection.
        if (previousButton != null && currentRecipeIndex == 0) {
            previousButton.setEnabled(false);
            if (currentRecipeIndex < currentRecipes.size() - 1) {
                nextButton.setEnabled(true);
            }
            return;
        }

        // detect if we are at the end of the recipe selection.
        if (nextButton != null && currentRecipeIndex == (currentRecipes.size() - 1)) {
            if (currentRecipeIndex > 0) {
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
        JPanel jPanelDescription = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // bottom panel
        jPanelBottom.add(jLabelResults);

        // description panel
        jEditorPane.setContentType("text/html");
        jEditorPane.setText(Processor.process("Recipe description will appear here", codigaMarkdownDecorator));
        jEditorPane.setEditable(false);

        jEditorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(hle.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        jPanelDescription.setBorder(BorderFactory.createEmptyBorder(0, CodigaIcons.Codiga_default_icon.getIconWidth() + 10, 0, 0));
        scrollPane.setPreferredSize(new Dimension(800 - (CodigaIcons.Codiga_default_icon.getIconWidth() + 10) * 2 , 200));
        scrollPane.setMinimumSize(new Dimension(800 - (CodigaIcons.Codiga_default_icon.getIconWidth() + 10) * 2, 200));
        jPanelDescription.add(scrollPane);

        // middle panel
        JLabel codigaLabel = new JLabel(CodigaIcons.Codiga_default_icon);
        codigaLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        searchTextfield = new JBTextField(40);

        /**
         * Listener to apply the recipe when pressing enter.
         */
        searchTextfield.addActionListener(l -> {
            if (currentRecipes != null && currentRecipeIndex <= currentRecipes.size()) {
                GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch insertedRecipe = currentRecipes.get(currentRecipeIndex);
                if (insertedRecipe != null) {
                    long recipeId = ((BigDecimal) insertedRecipe.id()).longValue();
                    currentRecipes = null;
                    currentRecipeIndex = 0;
                    codeInserted = false;
                    applyRecipe(event, recipeId, codeInsertions, highlighters, codigaApi);
                    windowWrapper.close();
                }
            }

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
                        if (thisRequestTimestamp == lastRequestTimestamp) {
                            removeAddedCode(event, codeInsertions, highlighters);
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
                        if (thisRequestTimestamp == lastRequestTimestamp) {
                            removeAddedCode(event, codeInsertions, highlighters);
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
            if(currentRecipes != null && currentRecipeIndex <= currentRecipes.size()) {
                GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch insertedRecipe = currentRecipes.get(currentRecipeIndex);
                if(insertedRecipe != null){
                    long recipeId = ((BigDecimal) insertedRecipe.id()).longValue();
                    currentRecipes = null;
                    currentRecipeIndex = 0;
                    codeInserted = false;
                    applyRecipe(event, recipeId, codeInsertions, highlighters, codigaApi);
                    windowWrapper.close();
                }
            }
        });

        nextButton = new JButton(AllIcons.General.ArrowRight);
        nextButton.setEnabled(false);
        nextButton.setMaximumSize(new Dimension(nextButton.getIcon().getIconWidth(),nextButton.getIcon().getIconHeight()));
        nextButton.setToolTipText("go to next suggestion");
        nextButton.addActionListener( a -> {
            if(currentRecipes != null && currentRecipeIndex < currentRecipes.size()) {
                removeAddedCode(event, codeInsertions, highlighters);
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
                removeAddedCode(event, codeInsertions, highlighters);
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
        jPanelMain.add(jPanelDescription);

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

        windowWrapper.getWindow().setPreferredSize(new Dimension(800, 300));
        windowWrapper.getWindow().setSize(new Dimension(800, 300));

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
