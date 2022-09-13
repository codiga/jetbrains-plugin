package io.codiga.plugins.jetbrains.actions.snippet_search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.serviceContainer.AlreadyDisposedException;
import com.intellij.util.Alarm;
import com.intellij.util.ui.AsyncProcessIcon;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.CodeInsertionContext;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier;
import io.codiga.plugins.jetbrains.topics.SnippetToolWindowFileChangeNotifier;
import io.codiga.plugins.jetbrains.topics.VisibilityKeyChangeNotifier;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getLanguageFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier.CODIGA_API_KEY_CHANGE_TOPIC;
import static io.codiga.plugins.jetbrains.topics.SnippetToolWindowFileChangeNotifier.CODIGA_NEW_FILE_SELECTED_TOPIC;
import static io.codiga.plugins.jetbrains.topics.VisibilityKeyChangeNotifier.CODIGA_VISIBILITY_CHANGE_TOPIC;
import static io.codiga.plugins.jetbrains.utils.LanguageUtils.getLanguageName;

/**
 * This implements the main tool window for the snippets.
 */
public class SnippetToolWindow {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final JPanel noRecipePanel = new JPanel();
    private final JPanel languageNotSupportedPanel = new JPanel();
    private final CodeInsertionContext codeInsertionContext = new CodeInsertionContext();
    private final Project project;
    private final ToolWindow toolWindow;

    AppSettingsState settings = AppSettingsState.getInstance();
    private JPanel mainPanel;
    private JTextField searchTerm;
    private JRadioButton radioAllSnippets;
    private JRadioButton radioPublicOnly;
    private JRadioButton radioPrivateOnly;
    private JCheckBox checkboxFavoritesOnly;
    private JPanel snippetsPanel;
    private JLabel loggedInLabel;
    private JPanel loadingPanel;
    private JScrollPane scrollPane;
    private JPanel scrollPanePanel;
    private JLabel languageLabel;
    private JPanel noEditorPanel;
    private boolean searchAllSnippets;
    private boolean searchPrivateSnippetsOnly;
    private boolean searchPublicSnippetsOnly;
    private boolean searchFavoriteSnippetsOnly;
    private boolean searchPrivateSnippetsOnlyEnabled;
    private boolean searchPublicSnippetsOnlyEnabled;
    private boolean searchFavoriteSnippetsOnlyEnabled;

    public SnippetToolWindow(ToolWindow toolWindow, Project project) {
        setDefaultValuesForSearchPreferences();
        getVisibilityFromSettings();
        Alarm searchTermAlarm = new Alarm();

        this.project = project;
        this.toolWindow = toolWindow;
        this.searchAllSnippets = !settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly();
        this.searchPrivateSnippetsOnly = settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly();
        this.searchPublicSnippetsOnly = !settings.getPrivateSnippetsOnly() && settings.getPublicSnippetsOnly();
        this.searchFavoriteSnippetsOnly = settings.getFavoriteSnippetsOnly();

        radioAllSnippets.addActionListener(e -> updateSearchPreferences(true, false, false, searchFavoriteSnippetsOnly));
        radioPrivateOnly.addActionListener(e -> updateSearchPreferences(false, true, false, searchFavoriteSnippetsOnly));
        radioPublicOnly.addActionListener(e -> updateSearchPreferences(false, false, true, searchFavoriteSnippetsOnly));
        checkboxFavoritesOnly.addActionListener(e -> updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, !searchFavoriteSnippetsOnly));

        /**
         * The search term is triggered a new search. We wait 500 ms before doing the search
         * in order to not hammer the backend with too many requests. We only make a
         * request if no key has not been typed within 500 ms.
         */
        searchTerm.getDocument().addDocumentListener(new DocumentListener() {
            private void updateResult() {

                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

                if (fileEditorManager == null || fileEditorManager.getSelectedEditor() == null) {
                    LOGGER.info("cannot find editor manager");
                    return;
                }

                VirtualFile virtualFile = fileEditorManager.getSelectedEditor().getFile();


                if (project == null || virtualFile == null) {
                    LOGGER.info("project or file are null");
                    return;
                }

                setLoading(false);
                searchTermAlarm.cancelAllRequests();
                searchTermAlarm.addRequest(() -> {
                    final String searchTermString = searchTerm.getText();
                    Optional<String> searchArgument = Optional.empty();
                    if (searchTermString.length() > 0) {
                        searchArgument = Optional.of(searchTermString);
                    }
                    updateEditor(project, virtualFile, searchArgument, false);
                }, 500);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateResult();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateResult();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // empty, nothing needed here
            }
        });


        loadingPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        loadingPanel.add(new AsyncProcessIcon("loading..."));
        loadingPanel.add(new JLabel(" loading..."));
        loadingPanel.setVisible(false);
        noRecipePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        noRecipePanel.add(new JLabel("no snippet found"));

        languageNotSupportedPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        languageNotSupportedPanel.add(new JLabel("language not supported"));

        snippetsPanel.setVisible(false);
        noEditorPanel.setVisible(false);

        updateUser();
        updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, searchFavoriteSnippetsOnly);

        /**
         * Listen to message bus if the user added their API keys. If that is the
         * case, we update the user and search preferences.
         */
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(CODIGA_API_KEY_CHANGE_TOPIC, new ApiKeyChangeNotifier() {
            @Override
            public void beforeAction(Object context) {
                // empty, nothing needed here
            }

            @Override
            public void afterAction(Object context) {
                updateUser();
                updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, searchFavoriteSnippetsOnly);
            }
        });

        ApplicationManager.getApplication().getMessageBus().connect().subscribe(CODIGA_VISIBILITY_CHANGE_TOPIC, new VisibilityKeyChangeNotifier() {
            @Override
            public void beforeAction(Object context) {
                // empty, nothing needed here
            }

            @Override
            public void afterAction(Object context) {
                updateUser();
                getVisibilityFromSettings();
                updateSearchPreferences(searchAllSnippets, searchPrivateSnippetsOnly, searchPublicSnippetsOnly, searchFavoriteSnippetsOnly);
            }
        });

        ApplicationManager.getApplication().getMessageBus().connect().subscribe(CODIGA_NEW_FILE_SELECTED_TOPIC, new SnippetToolWindowFileChangeNotifier() {
            @Override
            public void beforeAction(Object context) {
                // empty, nothing needed here
            }

            @Override
            public void afterAction(Object context) {
                updateUser();
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                if (fileEditorManager == null) {
                    return;
                }
                FileEditor fileEditor = fileEditorManager.getSelectedEditor();
                if (fileEditor == null) {
                    return;
                }
                VirtualFile virtualFile = fileEditor.getFile();
                if (virtualFile == null) {
                    return;
                }
                updateEditor(project, virtualFile, Optional.empty(), true);
            }
        });


        /**
         * Fill the content of the panel with the existing data
         * from the current editor if there is one opened.
         */
//        ApplicationManager.getApplication().executeOnPooledThread(() -> {
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
//            FileEditor fileEditor = fileEditorManager.getSelectedEditor();
//            if (fileEditor != null) {
//                VirtualFile virtualFile = fileEditor.getFile();
//                if (virtualFile != null) {
//                    updateEditor(project, virtualFile, Optional.empty(), true);
//                }
//            }
//        });

        searchTermAlarm.addRequest(() -> {
            try {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                FileEditor fileEditor = fileEditorManager.getSelectedEditor();
                if (fileEditor != null && fileEditor.getFile() != null) {
                    updateEditor(project, fileEditor.getFile(), Optional.empty(), false);
                }
            } catch (AlreadyDisposedException ade) {
                LOGGER.debug("Cannot create new panel", ade);
            }
        }, 500);
    }

    /**
     * By default, the scrollpanes are *very* slow. This function
     * fix the scroll pane passed as parameter and makes
     * the scroll speed faster.
     *
     * @param scrollpane
     */
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

    public void updateSearchPreferences(boolean _allSnippets, boolean _privateOnly, boolean _publicOnly, boolean _favoriteOnly) {
        this.searchFavoriteSnippetsOnly = _favoriteOnly;
        this.searchPublicSnippetsOnly = _publicOnly;
        this.searchAllSnippets = _allSnippets;
        this.searchPrivateSnippetsOnly = _privateOnly;
        radioAllSnippets.setSelected(this.searchAllSnippets);
        radioPrivateOnly.setSelected(this.searchPrivateSnippetsOnly);
        radioPublicOnly.setSelected(this.searchPublicSnippetsOnly);
        checkboxFavoritesOnly.setSelected(this.searchFavoriteSnippetsOnly);

        radioPrivateOnly.setEnabled(searchPrivateSnippetsOnlyEnabled);
        radioPublicOnly.setEnabled(searchPublicSnippetsOnlyEnabled);
        checkboxFavoritesOnly.setEnabled(searchFavoriteSnippetsOnlyEnabled);
    }

    /**
     * Set the default value for all checkboxes (e.g. public search by default).
     */
    private void setDefaultValuesForSearchPreferences() {
        searchAllSnippets = true;
        searchPrivateSnippetsOnly = false;
        searchPublicSnippetsOnly = false;
        searchFavoriteSnippetsOnly = false;
        searchPrivateSnippetsOnlyEnabled = false;
        searchPublicSnippetsOnlyEnabled = false;
        searchFavoriteSnippetsOnlyEnabled = false;
    }

    private void getVisibilityFromSettings() {
        this.searchAllSnippets = !settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly();
        this.searchPrivateSnippetsOnly = settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly();
        this.searchPublicSnippetsOnly = !settings.getPrivateSnippetsOnly() && settings.getPublicSnippetsOnly();
        this.searchFavoriteSnippetsOnly = settings.getFavoriteSnippetsOnly();
    }

    /**
     * Show a label that no editor is being selected.
     */
    public void updateNoEditor() {
        snippetsPanel.setVisible(false);
        loadingPanel.setVisible(false);
        noEditorPanel.setVisible(true);
    }

    /**
     * Check the user - if the user is logged, we offer to search
     * private, favorite and shared snippets. If not, only
     * public snippets are proposed.
     */
    private void updateUser() {
        Optional<String> username = codigaApi.getUsername();
        if (username.isPresent()) {
            searchPrivateSnippetsOnlyEnabled = true;
            searchPublicSnippetsOnlyEnabled = true;
            searchFavoriteSnippetsOnlyEnabled = true;
            String htmlLoginLabel = String.format("<html>Logged as <a href=\"https://app.codiga.io\">%s</a></html>", username.get());
            loggedInLabel.setText(htmlLoginLabel);
            loggedInLabel.addMouseListener(new LoginMouseListener());
        } else {
            setDefaultValuesForSearchPreferences();
            loggedInLabel.setText("not logged in");
        }
    }

    public JPanel getContent() {
        return mainPanel;
    }

    ;

    /**
     * Set the loading panel, hides all current snippets
     * This should then be reversed by the updateEditor
     * function when snippets are found.
     *
     * @param resetLanguage
     */
    public void setLoading(Boolean resetLanguage) {
        if (resetLanguage) {
            languageLabel.setText("loading");
        }
        snippetsPanel.setVisible(false);
        loadingPanel.setVisible(true);
        noEditorPanel.setVisible(false);
    }

    public void updateEditor(@NotNull Project project,
                             @NotNull VirtualFile virtualFile,
                             @NotNull Optional<String> term,
                             boolean resetSearch) {

        String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(project, virtualFile);
        java.util.List<String> dependencies = DependencyManagement.getInstance().getDependencies(project, virtualFile).stream().map(v -> v.getName()).collect(Collectors.toList());
        LanguageEnumeration languageEnumeration = getLanguageFromEditorForVirtualFile(virtualFile);

        /**
         * This is used when we open a new editor, we want to reset the search term.
         */
        if (resetSearch) {
            searchTerm.setText("");
        }

        if (languageEnumeration == LanguageEnumeration.UNKNOWN) {
            snippetsPanel.removeAll();
            snippetsPanel.add(languageNotSupportedPanel);
            snippetsPanel.revalidate();
            snippetsPanel.repaint();
            languageLabel.setText("unknown");
            loadingPanel.setVisible(false);
            snippetsPanel.setVisible(true);
            return;
        }

        languageLabel.setText(getLanguageName(languageEnumeration));

        Optional<Boolean> onlyPublic = Optional.empty();
        Optional<Boolean> onlyPrivate = Optional.empty();
        Optional<Boolean> onlyFavorite = Optional.empty();

        if (this.searchPublicSnippetsOnly) {
            onlyPublic = Optional.of(true);
            onlyPrivate = Optional.empty();
        }

        if (this.searchPrivateSnippetsOnly) {
            onlyPrivate = Optional.of(true);
            onlyPublic = Optional.empty();
        }

        if (this.searchFavoriteSnippetsOnly) {
            onlyFavorite = Optional.of(true);
        }

        // Make the request to get snippets from Codiga
        java.util.List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> snippets = codigaApi.getRecipesSemantic(term, dependencies, Optional.empty(), languageEnumeration, filename, onlyPublic, onlyPrivate, onlyFavorite);

        // Create the snippet panel.
        java.util.List<SnippetPanel> panels = snippets.stream().map(s -> new SnippetPanel(s, codeInsertionContext, toolWindow, project)).collect(Collectors.toList());

        snippetsPanel.removeAll();
        snippetsPanel.setLayout(new BoxLayout(snippetsPanel, BoxLayout.Y_AXIS));

        if (snippets.isEmpty()) {
            snippetsPanel.add(noRecipePanel);
        } else {
            panels.forEach(p -> {
                snippetsPanel.add(new JSeparator());
                snippetsPanel.add(p.getComponent());
            });
        }

        loadingPanel.setVisible(false);
        snippetsPanel.setVisible(true);
        noEditorPanel.setVisible(false);

        fixScrolling(scrollPane);
        // scroll back to the top
        SwingUtilities.invokeLater(() -> {
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
            snippetsPanel.revalidate();
            snippetsPanel.repaint();
        });
    }
}