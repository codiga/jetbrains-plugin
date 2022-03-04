package io.codiga.plugins.jetbrains.actions.shortcuts;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.shortcuts.listeners.RecipeListMouseListener;
import io.codiga.plugins.jetbrains.actions.shortcuts.listeners.RecipeListSelectionListener;
import io.codiga.plugins.jetbrains.actions.shortcuts.listeners.SearchDocumentListener;
import io.codiga.plugins.jetbrains.actions.shortcuts.listeners.SearchKeyListener;
import io.codiga.plugins.jetbrains.actions.shortcuts.model.RecipeListModel;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.model.CodeInsertion;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.*;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantListShortcuts extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private List<CodeInsertion> codeInsertions;
    private List<RangeHighlighter> highlighters;

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    // UI elements
    private WindowWrapper windowWrapper; // Window Wrapper that looks like IntelliJ



    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        LanguageEnumeration language = getLanguageFromEditorForEvent(event);
        String filename = getFilenameFromEditorForEvent(event);
        List<String> dependenciesName = getDependenciesFromEditorForEvent(event);
        highlighters = new ArrayList<>();
        codeInsertions = new ArrayList<CodeInsertion>();


        JPanel panelTop = new JPanel(); // contains search text
        JPanel panelMiddle = new JPanel();
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton learnMoreButton = new JButton("Learn More");
        learnMoreButton.setEnabled(false);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, CodigaIcons.Codiga_default_icon.getIconWidth() - 5));
        panelBottom.add(learnMoreButton);
        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = codigaApi.getRecipesForClientByShotcurt(Optional.empty(), dependenciesName, Optional.empty(), language, filename);

        JBList jbList = new JBList(new RecipeListModel(recipes));
        jbList.addListSelectionListener(new RecipeListSelectionListener(jbList, event, codeInsertions, highlighters, learnMoreButton));


        JLabel codigaLabel = new JLabel(CodigaIcons.Codiga_default_icon);


        JBTextField jTextField = new JBTextField(63);
        jTextField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, CodigaIcons.Codiga_default_icon.getIconWidth() + 10));

        jTextField.getDocument().addDocumentListener(new SearchDocumentListener(jbList, jTextField, learnMoreButton, recipes));

        jTextField.setTextToTriggerEmptyTextStatus("enter search");
        jTextField.setToolTipText("Search term for recipes");

        panelTop.add(codigaLabel);
        panelTop.add(jTextField);


        jbList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut v = (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut) value;
            return new JBLabel(String.format("%s: %s", v.shortcut() ,v.name()));

        });
        jbList.setEmptyText("No recipes found");
        JBScrollPane jbScrollPane = new JBScrollPane(jbList);
        jbScrollPane.setPreferredSize(new Dimension(800 - (CodigaIcons.Codiga_default_icon.getIconWidth() + 10) * 2 , 200));
        panelMiddle.add(jbScrollPane);


        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        jPanelMain.add(panelTop);
        jPanelMain.add(panelMiddle);
        jPanelMain.add(panelBottom);


        // Build the main window to keep it with an IntelliJ style
        windowWrapper = new WindowWrapperBuilder(WindowWrapper.Mode.FRAME, jPanelMain)
                .setProject(event.getDataContext().getData(LangDataKeys.PROJECT))
                .setTitle("Codiga Coding Assistant - Shortcuts")
                .setDimensionServiceKey("Codiga.Shortcut") // key to remember the dimension on the local client
                .build();

        windowWrapper.getWindow().setPreferredSize(new Dimension(780, 290));
        windowWrapper.getWindow().setSize(new Dimension(780, 290));


        // Add listeners
        jTextField.addKeyListener(new SearchKeyListener(event, jbList, codeInsertions, highlighters, windowWrapper, codigaApi));
        jbList.addMouseListener(new RecipeListMouseListener(event, jbList, codeInsertions, highlighters, windowWrapper, codigaApi));

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
