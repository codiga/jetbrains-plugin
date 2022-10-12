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
import io.codiga.plugins.jetbrains.SnippetVisibility;
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
import java.util.List;
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
    private SnippetVisibility snippetVisibility;
    private boolean searchPrivateSnippetsOnlyEnabled;
    private boolean searchPublicSnippetsOnlyEnabled;
    private boolean searchFavoriteSnippetsOnlyEnabled;

    public SnippetToolWindow(ToolWindow toolWindow, Project project) {
        setDefaultValuesForSearchPreferences();
        Alarm searchTermAlarm = new Alarm();

        this.project = project;
        this.toolWindow = toolWindow;
        initVisibilityFromSettings();
        radioAllSnippets.addActionListener(e -> updateSearchPreferences(true, false, false, snippetVisibility.isOnlyFavorite()));
        radioPrivateOnly.addActionListener(e -> updateSearchPreferences(false, true, false, snippetVisibility.isOnlyFavorite()));
        radioPublicOnly.addActionListener(e -> updateSearchPreferences(false, false, true, snippetVisibility.isOnlyFavorite()));
        checkboxFavoritesOnly.addActionListener(e -> updateSearchPreferences(snippetVisibility.isAllSnippets(), snippetVisibility.isOnlyPrivate(), snippetVisibility.isOnlyPublic(), !snippetVisibility.isOnlyFavorite()));

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
        updateSearchPreferences();

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
                updateSearchPreferences();
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
                initVisibilityFromSettings();
                updateSearchPreferences();
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

                // Check if project still active
                if (project.isDisposed()) {
                    LOGGER.info("Project already disposed");
                    return;
                }
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

    private void updateSearchPreferences() {
        updateSearchPreferences(snippetVisibility.isAllSnippets(), snippetVisibility.isOnlyPrivate(), snippetVisibility.isOnlyPublic(), snippetVisibility.isOnlyFavorite());
    }

    private void updateSearchPreferences(boolean _allSnippets, boolean _privateOnly, boolean _publicOnly, boolean _favoriteOnly) {
        snippetVisibility.setVisibilities(_allSnippets, _privateOnly, _publicOnly, _favoriteOnly);
        radioAllSnippets.setSelected(_allSnippets);
        radioPrivateOnly.setSelected(_privateOnly);
        radioPublicOnly.setSelected(_publicOnly);
        checkboxFavoritesOnly.setSelected(_favoriteOnly);

        radioPrivateOnly.setEnabled(searchPrivateSnippetsOnlyEnabled);
        radioPublicOnly.setEnabled(searchPublicSnippetsOnlyEnabled);
        checkboxFavoritesOnly.setEnabled(searchFavoriteSnippetsOnlyEnabled);
    }

    /**
     * Set the default value for all checkboxes (e.g. public search by default).
     */
    private void setDefaultValuesForSearchPreferences() {
        searchPrivateSnippetsOnlyEnabled = false;
        searchPublicSnippetsOnlyEnabled = false;
        searchFavoriteSnippetsOnlyEnabled = false;
    }

    private void initVisibilityFromSettings() {
        this.snippetVisibility = new SnippetVisibility(
            !settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly(),
            settings.getPrivateSnippetsOnly() && !settings.getPublicSnippetsOnly(),
            !settings.getPrivateSnippetsOnly() && settings.getPublicSnippetsOnly(),
            settings.getFavoriteSnippetsOnly());
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
            snippetVisibility.setVisibilities(true, false, false, false);
            setDefaultValuesForSearchPreferences();
            loggedInLabel.setText("not logged in");
        }
    }

    public JPanel getContent() {
        return mainPanel;
    }

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

        var visibilityForQuery = snippetVisibility.prepareForQuery();

        // Make the request to get snippets from Codiga
        List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> snippets =
            codigaApi.getRecipesSemantic(term, dependencies, Optional.empty(), languageEnumeration, filename, visibilityForQuery.getOnlyPublic(), visibilityForQuery.getOnlyPrivate(), visibilityForQuery.getOnlyFavorite());

        // Create the snippet panel.
        List<SnippetPanel> panels = snippets.stream().map(s -> new SnippetPanel(s, codeInsertionContext, project)).collect(Collectors.toList());

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
        snippetsPanel.setVisible(false);
        noEditorPanel.setVisible(false);

        fixScrolling(scrollPane);
        // scroll back to the top
        SwingUtilities.invokeLater(() -> {
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
            snippetsPanel.revalidate();
            snippetsPanel.repaint();
            //Notes regarding rendering the code snippet EditorTextFields:
            //1. Making the snippets panel not visible before repainting, and visible again after it,
            // helps to render all (not just the first) code snippet fields upon GUI update.
            //2. 'setVisible()' must be inside 'invokeLater()' and after repainting,
            // otherwise it'd be called before repaint, and the rendering would be broken.
            //3. Leave 'SwingUtilities.invokeLater()', using 'Application.invokeLater()' the rendering is broken.
            snippetsPanel.setVisible(true);
        });
    }
}
