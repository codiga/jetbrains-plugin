package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Alarm;
import com.intellij.util.ui.AsyncProcessIcon;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getLanguageFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;

public class SnippetToolWindow {
    private JPanel mainPanel;
    private JTextField searchTerm;
    private JRadioButton radioAllSnippets;
    private JRadioButton radioPublicOnly;
    private JRadioButton radioPrivateOnly;
    private JCheckBox checkboxFavoritesOnly;
    private JPanel scrollPanePanel;
    private JPanel snippetsPanel;
    private JLabel loggedInLabel;
    private JPanel loadingPanel;
    private JScrollPane scrollPane;
    private JPanel snippetsScrollPanel;
    private boolean searchAllSnippets;
    private boolean searchPrivateSnippetsOnly;
    private boolean searchPublicSnippetsOnly;
    private boolean searchFavoriteSnippetsOnly;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final JPanel noRecipePanel = new JPanel();
    private final JPanel languageNotSupportedPanel = new JPanel();

    public void updateSearchPreferences(boolean _allSnippets, boolean _privateOnly, boolean _publicOnly, boolean _favoriteOnly) {
        this.searchFavoriteSnippetsOnly = _favoriteOnly;
        this.searchPublicSnippetsOnly = _publicOnly;
        this.searchAllSnippets = _allSnippets;
        this.searchPrivateSnippetsOnly = _privateOnly;
        radioAllSnippets.setSelected(this.searchAllSnippets);
        radioPrivateOnly.setSelected(this.searchPrivateSnippetsOnly);
        radioPublicOnly.setSelected(this.searchPublicSnippetsOnly);
        checkboxFavoritesOnly.setSelected(this.searchFavoriteSnippetsOnly);
    }

    public SnippetToolWindow(ToolWindow toolWindow) {
        searchAllSnippets = true;
        searchPrivateSnippetsOnly = false;
        searchPublicSnippetsOnly = false;
        searchFavoriteSnippetsOnly = false;

//        snippetsPanel.setLayout(new BoxLayout(snippetsPanel, BoxLayout.Y_AXIS));
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
//        snippetsPanel.add(new SnippetPanel().getComponent());
        Alarm searchTermAlarm = new Alarm();

        radioAllSnippets.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSearchPreferences(true, false, false, searchFavoriteSnippetsOnly);
            }
        });

        radioPrivateOnly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSearchPreferences(false, true, false, searchFavoriteSnippetsOnly);
            }
        });

        radioPublicOnly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSearchPreferences(false, false, true, searchFavoriteSnippetsOnly);
            }
        });


        checkboxFavoritesOnly.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, !searchFavoriteSnippetsOnly);
            }
        });


        searchTerm.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                loadingPanel.setVisible(false);
                searchTermAlarm.cancelAllRequests();
                searchTermAlarm.addRequest(() -> {
                    final String searchTermString = searchTerm.getText();
                    Optional<String> searchArgument = Optional.empty();
                    LOGGER.info("new search term: " + searchTermString);
                    if(searchTermString.length() > 0) {
                        searchArgument = Optional.of(searchTermString);
                    }
                    Project project = SnippetToolWindowFileEditorManagerListener.getCurrentProject();
                    VirtualFile virtualFile = SnippetToolWindowFileEditorManagerListener.getCurrentVirtualFile();

                    if (project == null || virtualFile == null) {
                        LOGGER.info("project or file are null");
                        return;
                    }

                    updateEditor(project, virtualFile, searchArgument, false);
                }, 500);

            }
        });

        loadingPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        loadingPanel.add(new AsyncProcessIcon("loading..."));
        loadingPanel.add(new JLabel(" loading..."));

        noRecipePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        noRecipePanel.add(new JLabel("no snippet found"));

        languageNotSupportedPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        languageNotSupportedPanel.add(new JLabel("language not supported"));

        scrollPane.setAutoscrolls(false);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setBlockIncrement(16);

        updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, searchFavoriteSnippetsOnly);

    }

    public static void fixScrolling(JScrollPane scrollpane) {
        JLabel systemLabel = new JLabel();
        FontMetrics metrics = systemLabel.getFontMetrics(systemLabel.getFont());
        int lineHeight = metrics.getHeight();
        int charWidth = metrics.getMaxAdvance();

        JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
        JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
        int verticalIncrement = systemVBar.getUnitIncrement();
        int horizontalIncrement = systemHBar.getUnitIncrement();

        scrollpane.getVerticalScrollBar().setUnitIncrement(lineHeight * verticalIncrement);
        scrollpane.getHorizontalScrollBar().setUnitIncrement(charWidth * horizontalIncrement);
    }

    public JPanel getContent() { return mainPanel;};

    public void updateEditor(@NotNull Project project, @NotNull VirtualFile virtualFile, Optional<String> term, boolean resetSearch) {

        String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, virtualFile);
        java.util.List<String> dependencies = DependencyManagement.getInstance().getDependencies(project, virtualFile).stream().map(v -> v.getName()).collect(Collectors.toList());
        LanguageEnumeration languageEnumeration = getLanguageFromEditorForVirtualFile(virtualFile);

        if (resetSearch) {
            searchTerm.setText("");
        }

        if (languageEnumeration == LanguageEnumeration.UNKNOWN) {
            snippetsPanel.removeAll();
            snippetsPanel.add(languageNotSupportedPanel);
            snippetsPanel.revalidate();
            snippetsPanel.repaint();
            return;
        }


        java.util.List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> snippets = codigaApi.getRecipesSemantic(term, dependencies, Optional.empty(), languageEnumeration, filename, Optional.empty(), Optional.empty(), Optional.empty());

        LOGGER.info("found " + snippets.size() + " recipes");

        java.util.List<SnippetPanel> panels =  snippets.stream().map(snippet -> {
            LOGGER.info(snippet.name());
            return new SnippetPanel(snippet);
        }).collect(Collectors.toList());

        snippetsPanel.removeAll();
        snippetsPanel.setLayout(new BoxLayout(snippetsPanel, BoxLayout.Y_AXIS));

        if(snippets.isEmpty()) {
            snippetsPanel.add(noRecipePanel);
        } else {
            panels.forEach(p -> {
                snippetsPanel.add(p.getComponent());
            });
        }

        loadingPanel.setVisible(false);
        snippetsPanel.revalidate();
        snippetsPanel.repaint();
        scrollPane.getViewport().setViewPosition(new Point(0,0 ));

       // fixScrolling(scrollPane);
        // scroll back to the top
//        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0,0 )));
    }
}