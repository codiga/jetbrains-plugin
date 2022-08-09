package io.codiga.plugins.jetbrains.settings.application;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.ui.DialogApiStatus;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Optional;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.ui.UIConstants.*;

/**
 * Represents the view of the settings (adding JPanel/buttons/text)
 * for the plugin at the application level.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField apiToken = new JBTextField();
    private final JCheckBox useCompletationCheckbox;

    private JBRadioButton snippetsVisibilityAll = new JBRadioButton();
    private JBRadioButton snippetsVisibilityPublic = new JBRadioButton();
    private JBRadioButton snippetsVisibilityPrivate = new JBRadioButton();
    private JBCheckBox snippetsVisibilityFavoriteOnly = new JBCheckBox();
    private JBCheckBox useInlineCompletion = new JBCheckBox();
    private boolean useCompletion;
    private boolean snippetsPublicOnly;
    private boolean snippetsPrivateOnly;
    private boolean snippetsFavoriteOnly;
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public AppSettingsComponent() {
        /**
         * Use the following code only to debug and find all supported languages
         *         Collection<Language> allLanguages = com.intellij.lang.Language.getRegisteredLanguages();
         *         System.out.println("showing langauges");
         *         for(Language language: allLanguages){
         *             System.out.println(language.getID());
         *         }
         */

        BorderLayoutPanel p = new BorderLayoutPanel();
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        JButton buttonTestConnection = new JButton(SETTINGS_TEST_API_BUTTON_TEXT);
        JButton buttonGetApiKeys = new JButton(SETTINGS_GET_API_TOKEN_BUTTON_TEXT);


        useCompletationCheckbox = new JCheckBox();

        useCompletationCheckbox.addActionListener(event -> {
            LOGGER.debug("setting isDisabled to" + useCompletationCheckbox.isSelected());
            useCompletion = useCompletationCheckbox.isSelected();
        });

        snippetsVisibilityAll.addActionListener(event -> {
            if (snippetsVisibilityAll.isSelected()) {
                this.snippetsPublicOnly = false;
                this.snippetsPrivateOnly = false;
            }
            setSnippetsVisiliblity(this.snippetsPrivateOnly, this.snippetsPublicOnly, this.snippetsFavoriteOnly);
        });

        snippetsVisibilityPublic.addActionListener(event -> {
            if (snippetsVisibilityPublic.isSelected()) {
                this.snippetsPublicOnly = true;
                this.snippetsPrivateOnly = false;
            }
            setSnippetsVisiliblity(this.snippetsPrivateOnly, this.snippetsPublicOnly, this.snippetsFavoriteOnly);
        });

        snippetsVisibilityPrivate.addActionListener(event -> {
            if (snippetsVisibilityPrivate.isSelected()) {
                this.snippetsPublicOnly = false;
                this.snippetsPrivateOnly = true;
            }
            setSnippetsVisiliblity(this.snippetsPrivateOnly, this.snippetsPublicOnly, this.snippetsFavoriteOnly);
        });


        snippetsVisibilityFavoriteOnly.addActionListener(event -> {
            this.snippetsFavoriteOnly = snippetsVisibilityFavoriteOnly.isSelected();
            setSnippetsVisiliblity(this.snippetsPrivateOnly, this.snippetsPublicOnly, this.snippetsFavoriteOnly);
        });


        buttonGetApiKeys.addActionListener(arg0 -> {
            try {
                Desktop.getDesktop().browse(new URL("https://app.codiga.io/api-tokens").toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonTestConnection.addActionListener(arg0 -> {
            Optional<String> username = codigaApi.getUsername();
            if(username.isPresent()) {
                new DialogApiStatus(API_STATUS_TEXT_OK).showAndGet();
            } else {
                new DialogApiStatus(API_STATUS_TEXT_FAIL).showAndGet();
            }
        });
        buttonsPanel.add(buttonGetApiKeys);
        buttonsPanel.add(buttonTestConnection);
        p.addToRight(buttonsPanel);
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(SETTINGS_API_TOKEN_LABEL), apiToken, 1, false)
                .addLabeledComponent(new JBLabel("            "), new JBLabel(" Add your Codiga API keys to use your recipes in your IDE."), 1, false)
                .addComponent(p, 0)
                .addLabeledComponent(this.useCompletationCheckbox, new JBLabel(SETTINGS_ENABLED_COMPLETION))
            .addLabeledComponent(useInlineCompletion, new JBLabel("Enable Coding Assistant with inline completion"))
                .addSeparator(1)

                .addComponent(new JLabel(SETTINGS_SNIPPETS_VISIBILITY_PARAMETERS), 1)
                .addLabeledComponent(snippetsVisibilityAll, new JBLabel("All Snippets"))
                .addLabeledComponent(snippetsVisibilityPublic, new JBLabel("Public Snippets Only"))
                .addLabeledComponent(snippetsVisibilityPrivate, new JBLabel("Private Snippets Only"))
                .addLabeledComponent(snippetsVisibilityFavoriteOnly, new JBLabel("Favorite Snippets Only"))
                .addVerticalGap(5)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return apiToken;
    }


    @NotNull
    public String getApiToken() {
        return apiToken.getText();
    }

    public void setApiToken(@NotNull String newText) {
        apiToken.setText(newText);
    }

    public boolean useCompletion(){
        return this.useCompletion;
    }
    public boolean usePublicSnippetsOnly() { return this.snippetsPublicOnly; }
    public boolean usePrivateSnippetsOnly() { return this.snippetsPrivateOnly; }
    public boolean useFavoriteSnippetsOnly() { return this.snippetsFavoriteOnly; }

    public void setUseEnabledCheckbox(Boolean b) {
        if (this.useCompletationCheckbox != null) {
            this.useCompletion = b;
            this.useCompletationCheckbox.setSelected(b);
        }
    }

    public void setSnippetsVisiliblity(boolean privateOnly, boolean publicOnly, boolean favoriteOnly) {
        LOGGER.debug("private: " + privateOnly);
        LOGGER.debug("public: " + publicOnly);
        LOGGER.debug("favorite: " + favoriteOnly);
        this.snippetsPublicOnly = publicOnly;
        this.snippetsFavoriteOnly = favoriteOnly;
        this.snippetsPrivateOnly = privateOnly;
        if(this.snippetsVisibilityAll == null || this.snippetsVisibilityPrivate == null || this.snippetsVisibilityPublic == null || this.snippetsVisibilityFavoriteOnly == null) {
            return;

        }

        if(!privateOnly && !publicOnly){
            this.snippetsVisibilityAll.setSelected(true);
            this.snippetsVisibilityPublic.setSelected(false);
            this.snippetsVisibilityPrivate.setSelected(false);
        }
        if(!privateOnly && publicOnly){
            this.snippetsVisibilityAll.setSelected(false);
            this.snippetsVisibilityPublic.setSelected(true);
            this.snippetsVisibilityPrivate.setSelected(false);
        }
        if(privateOnly && !publicOnly){
            this.snippetsVisibilityAll.setSelected(false);
            this.snippetsVisibilityPublic.setSelected(false);
            this.snippetsVisibilityPrivate.setSelected(true);
        }


        this.snippetsVisibilityFavoriteOnly.setSelected(favoriteOnly);

    }
}

