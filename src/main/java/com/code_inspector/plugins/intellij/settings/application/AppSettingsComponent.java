package com.code_inspector.plugins.intellij.settings.application;

import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.ui.DialogApiStatus;
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

import static com.code_inspector.plugins.intellij.ui.UIConstants.*;

/**
 * Represents the view of the settings (adding JPanel/buttons/text)
 * for the plugin at the application level.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField apiToken = new JBTextField();
    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);

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

        buttonGetApiKeys.addActionListener(arg0 -> {
            try {
                Desktop.getDesktop().browse(new URL("https://frontend.code-inspector.com/api-tokens").toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonTestConnection.addActionListener(arg0 -> {
            Optional<String> username = codeInspectorApi.getUsername();
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
                .addComponent(p, 0)
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
}

