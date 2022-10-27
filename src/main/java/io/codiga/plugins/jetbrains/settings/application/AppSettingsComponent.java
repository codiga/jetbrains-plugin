package io.codiga.plugins.jetbrains.settings.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.components.BorderLayoutPanel;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.ui.DialogApiStatus;
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

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final JPanel myMainPanel;
    private final JPasswordField apiToken = new JPasswordField(40);
    private final JCheckBox useCompletionCheckbox;
    private JBRadioButton snippetsVisibilityAll = new JBRadioButton(SETTINGS_SNIPPETS_VISIBILITY_ALL_SNIPPETS);
    private JBRadioButton snippetsVisibilityPublic = new JBRadioButton(SETTINGS_SNIPPETS_VISIBILITY_PUBLIC_SNIPPETS_ONLY);
    private JBRadioButton snippetsVisibilityPrivate = new JBRadioButton(SETTINGS_SNIPPETS_VISIBILITY_PRIVATE_ONLY);
    private JBCheckBox snippetsVisibilityFavoriteOnly = new JBCheckBox(SETTINGS_SNIPPETS_VISIBILITY_FAVORITE_ONLY);
    private JBCheckBox useInlineCompletionCheckbox = new JBCheckBox(SETTINGS_ENABLED_INLINE_COMPLETION);
    private JBCheckBox codigaEnabledCheckbox = new JBCheckBox(SETTINGS_ENABLED_CODIGA);
    private boolean useCompletion;
    private boolean useInlineCompletion;
    private boolean snippetsPublicOnly;
    private boolean snippetsPrivateOnly;
    private boolean snippetsFavoriteOnly;
    private boolean codigaEnabled;

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

        //The button group is responsible for making sure that only one
        // radio button in that group can be selected at a time
        var snippetVisibilityGroup = new ButtonGroup();
        snippetVisibilityGroup.add(snippetsVisibilityAll);
        snippetVisibilityGroup.add(snippetsVisibilityPublic);
        snippetVisibilityGroup.add(snippetsVisibilityPrivate);

        useCompletionCheckbox = new JCheckBox(SETTINGS_ENABLED_COMPLETION);

        useCompletionCheckbox.addActionListener(event -> {
            LOGGER.debug("setting isDisabled to" + useCompletionCheckbox.isSelected());
            useCompletion = useCompletionCheckbox.isSelected();
        });

        snippetsVisibilityAll.addActionListener(event -> {
            if (snippetsVisibilityAll.isSelected()) {
                this.snippetsPublicOnly = false;
                this.snippetsPrivateOnly = false;
            }
        });

        snippetsVisibilityPublic.addActionListener(event -> {
            if (snippetsVisibilityPublic.isSelected()) {
                this.snippetsPublicOnly = true;
                this.snippetsPrivateOnly = false;
            }
        });

        snippetsVisibilityPrivate.addActionListener(event -> {
            if (snippetsVisibilityPrivate.isSelected()) {
                this.snippetsPublicOnly = false;
                this.snippetsPrivateOnly = true;
            }
        });


        snippetsVisibilityFavoriteOnly.addActionListener(event ->
            this.snippetsFavoriteOnly = snippetsVisibilityFavoriteOnly.isSelected());

        useInlineCompletionCheckbox.addActionListener(event ->
            this.useInlineCompletion = useInlineCompletionCheckbox.isSelected());

        codigaEnabledCheckbox.addActionListener(event -> {
            this.codigaEnabled = codigaEnabledCheckbox.isSelected();

            this.useInlineCompletionCheckbox.setEnabled(this.codigaEnabled);
            this.useCompletionCheckbox.setEnabled(this.codigaEnabled);
        });


        buttonGetApiKeys.addActionListener(arg0 -> {
            try {
                Desktop.getDesktop().browse(new URL("https://app.codiga.io/api-tokens").toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonTestConnection.addActionListener(arg0 -> {
            final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
            Optional<String> username = codigaApi.getUsername();
            if (username.isPresent()) {
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
            .addLabeledComponent(this.codigaEnabledCheckbox, new JBLabel(SETTINGS_ENABLED_CODIGA))
            .addLabeledComponent(this.useCompletationCheckbox, new JBLabel(SETTINGS_ENABLED_COMPLETION))
            .addLabeledComponent(useInlineCompletionCheckbox, new JBLabel(SETTINGS_ENABLED_INLINE_COMPLETION))
            .addSeparator(1)

            .addComponent(new JLabel(SETTINGS_SNIPPETS_VISIBILITY_PARAMETERS), 1)
            .addLabeledComponent(snippetsVisibilityAll, new JBLabel(SETTINGS_SNIPPETS_VISIBILITY_ALL_SNIPPETS))
            .addLabeledComponent(snippetsVisibilityPublic, new JBLabel(SETTINGS_SNIPPETS_VISIBILITY_PUBLIC_SNIPPETS_ONLY))
            .addLabeledComponent(snippetsVisibilityPrivate, new JBLabel(SETTINGS_SNIPPETS_VISIBILITY_PRIVATE_ONLY))
            .addLabeledComponent(snippetsVisibilityFavoriteOnly, new JBLabel(SETTINGS_SNIPPETS_VISIBILITY_FAVORITE_ONLY))
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
        if (apiToken.getPassword() == null){
            return "";
        }
        if (apiToken.getPassword().length == 0){
            return "";
        }
        return new String(apiToken.getPassword());
    }

    public void setApiToken(@NotNull String newText) {
        apiToken.setText(newText);
    }

    public boolean useCompletion() {
        return this.useCompletion;
    }

    public boolean usePublicSnippetsOnly() {
        return this.snippetsPublicOnly;
    }

    public boolean usePrivateSnippetsOnly() {
        return this.snippetsPrivateOnly;
    }

    public boolean useFavoriteSnippetsOnly() {
        return this.snippetsFavoriteOnly;
    }

    public boolean useInlineCompletion() {
        return this.useInlineCompletion;
    }

    public boolean isCodigaEnabled() {
        return this.codigaEnabled;
    }

    public void setCodigaEnabled(Boolean b) {
        if (this.codigaEnabledCheckbox != null) {
            this.codigaEnabled = b;
            this.codigaEnabledCheckbox.setSelected(b);
        }
    }

    public void setUseInlineComplextion(Boolean b) {
        this.useInlineCompletion = b;
        this.useInlineCompletionCheckbox.setSelected(b);
        this.useInlineCompletionCheckbox.setEnabled(codigaEnabled);
    }

    public void setUseEnabledCheckbox(Boolean b) {
        if (this.useCompletionCheckbox != null) {
            this.useCompletion = b;
            this.useCompletionCheckbox.setSelected(b);
            this.useCompletionCheckbox.setEnabled(codigaEnabled);
        }
    }

    public void setSnippetsVisibility(boolean privateOnly, boolean publicOnly, boolean favoriteOnly) {
        LOGGER.debug("private: " + privateOnly);
        LOGGER.debug("public: " + publicOnly);
        LOGGER.debug("favorite: " + favoriteOnly);
        this.snippetsPublicOnly = publicOnly;
        this.snippetsFavoriteOnly = favoriteOnly;
        this.snippetsPrivateOnly = privateOnly;
        if (this.snippetsVisibilityAll == null
            || this.snippetsVisibilityPrivate == null
            || this.snippetsVisibilityPublic == null
            || this.snippetsVisibilityFavoriteOnly == null) {
            return;
        }

        if (!privateOnly && !publicOnly) {
            this.snippetsVisibilityAll.setSelected(true);
        }
        if (!privateOnly && publicOnly) {
            this.snippetsVisibilityPublic.setSelected(true);
        }
        if (privateOnly && !publicOnly) {
            this.snippetsVisibilityPrivate.setSelected(true);
        }

        this.snippetsVisibilityFavoriteOnly.setSelected(favoriteOnly);
    }
}
