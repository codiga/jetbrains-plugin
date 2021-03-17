package com.code_inspector.plugins.intellij.settings.application;

import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.ui.DialogApiStatus;
import com.intellij.openapi.components.ServiceManager;
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
    private final JBTextField accessKey = new JBTextField();
    private final JBTextField secretKey = new JBTextField();
    private final CodeInspectorApi codeInspectorApi = ServiceManager.getService(CodeInspectorApi.class);

    public AppSettingsComponent() {
        BorderLayoutPanel p = new BorderLayoutPanel();
        JPanel buttonsPanel = new JPanel(new FlowLayout());

        JButton buttonTestConnection = new JButton(SETTINGS_TEST_API_BUTTON_TEXT);
        JButton buttonGetApiKeys = new JButton(SETTINGS_GET_API_KEYS_BUTTON_TEXT);

        buttonGetApiKeys.addActionListener(arg0 -> {
            try {
                Desktop.getDesktop().browse(new URL("https://frontend.code-inspector.com/account/profile").toURI());
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
                .addLabeledComponent(new JBLabel(SETTINGS_ACCESS_KEY_LABEL), accessKey, 1, false)
                .addLabeledComponent(new JBLabel(SETTINGS_SECRET_KEY_LABEL), secretKey, 1, false)
                .addComponent(p, 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return accessKey;
    }

    @NotNull
    public String getAccessKey() {
        return accessKey.getText();
    }

    public void setAccessKey(@NotNull String newText) {
        accessKey.setText(newText);
    }

    @NotNull
    public String getSecretKey() {
        return secretKey.getText();
    }

    public void setSecretKey(@NotNull String newText) {
        secretKey.setText(newText);
    }

}

