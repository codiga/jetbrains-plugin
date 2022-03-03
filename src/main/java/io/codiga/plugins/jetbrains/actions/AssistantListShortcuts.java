package io.codiga.plugins.jetbrains.actions;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import icons.CodigaIcons;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.*;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * This action is used to use a recipe. It is invoked by the user when in an editor.
 */
public class AssistantListShortcuts extends AnAction {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    // UI elements
    private WindowWrapper windowWrapper; // Window Wrapper that looks like IntelliJ


    class RecipeListModel implements ListModel {
        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = new ArrayList<>();

        public RecipeListModel(List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> newRecipes) {
            this.recipes.clear();
            this.recipes.addAll(newRecipes);
            sortList();
        }


        public void setRecipes(List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> newRecipes){
            recipes = newRecipes;
        }


        @Override
        public int getSize() {
            return recipes.size();
        }

        @Override
        public Object getElementAt(int index) {
            return recipes.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {

        }

        @Override
        public void removeListDataListener(ListDataListener l) {

        }


        public boolean hasRecipe(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
            return recipes.contains(recipe);
        }


        public boolean remove(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
            return recipes.remove(recipe);
        }

        public boolean add(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe) {
            return recipes.add(recipe);
        }

        public void sortList() {
            recipes.sort(new Comparator<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut>() {
                @Override
                public int compare(GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut o1, GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut o2) {
                    return o1.shortcut().compareTo(o2.shortcut());
                }
            });
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile virtualFile = event.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        PsiFile psiFile = event.getDataContext().getData(LangDataKeys.PSI_FILE);
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

        String filename = null;

        if (psiFile.getVirtualFile() != null)
        {
            filename = psiFile.getVirtualFile().getName();
        }

        JPanel panelTop = new JPanel(); // contains search text
        JPanel panelBottom = new JPanel();
        List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> recipes = codigaApi.getRecipesForClientByShotcurt(Optional.empty(), ImmutableList.of(), Optional.empty(), language, filename);

        JBList jbList = new JBList(new RecipeListModel(recipes));

        JLabel codigaLabel = new JLabel(CodigaIcons.Codiga_default_icon);


        JBTextField jTextField = new JBTextField(63);
        jTextField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, CodigaIcons.Codiga_default_icon.getIconWidth() + 10));

        jTextField.getDocument().addDocumentListener(new DocumentListener() {

            private void filterElements() {
                RecipeListModel model = (RecipeListModel)jbList.getModel();
                String term = jTextField.getText().toLowerCase();
                if(term.isEmpty()) {
                    for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe: recipes) {
                        if(!model.hasRecipe(recipe)) {
                            model.add(recipe);
                        }
                    }
                } else {
                    for (GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut recipe: recipes) {
                        boolean recipeMatch = recipe.name().toLowerCase().contains(term) || recipe.description().toLowerCase().contains(term) || recipe.keywords().contains(term) || recipe.shortcut().toLowerCase().contains(term);
                        if(recipeMatch) {
                            if(!model.hasRecipe(recipe)){
                                model.add(recipe);
                            }
                        } else {
                            if (model.hasRecipe(recipe)){
                                model.remove(recipe);
                            }
                        }

                    }

                }
                if(jbList.getItemsCount() > 0) {
                    jbList.setSelectedIndex(0);
                }
                model.sortList();
                jbList.repaint();

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterElements();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterElements();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        jTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());
                if (e.getKeyCode() == 38 || e.getKeyCode() == 33) { // 38 is up arrow key ; 33 is page up
                    int newIndex = jbList.getSelectedIndex() - 1;
                    if (newIndex >= 0) {
                        jbList.setSelectedIndex(newIndex);
                    }
                    jbList.ensureIndexIsVisible(jbList.getSelectedIndex());
                }
                if (e.getKeyCode() == 40 || e.getKeyCode() == 34) { // 40 down arrow key ; 34 is page down
                    int newIndex = jbList.getSelectedIndex() + 1;
                    if (newIndex < jbList.getItemsCount()) {
                        jbList.setSelectedIndex(newIndex);
                    }

                    jbList.ensureIndexIsVisible(jbList.getSelectedIndex());
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
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
        panelBottom.add(jbScrollPane);


        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        jPanelMain.add(panelTop);
        jPanelMain.add(panelBottom);


        // Build the main window to keep it with an IntelliJ style
        windowWrapper = new WindowWrapperBuilder(WindowWrapper.Mode.FRAME, jPanelMain)
                .setProject(event.getDataContext().getData(LangDataKeys.PROJECT))
                .setTitle("Codiga Coding Assistant")
                .setDimensionServiceKey("Codiga.Shortcut") // key to remember the dimension on the local client
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
