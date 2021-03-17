package com.code_inspector.plugins.intellij.settings.application;

import com.code_inspector.plugins.intellij.topics.ApiKeyChangeNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.code_inspector.plugins.intellij.topics.ApiKeyChangeNotifier.CODE_INSPECTOR_API_KEY_CHANGE_TOPIC;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    final ApiKeyChangeNotifier apiKeyChangeNotifier =
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CODE_INSPECTOR_API_KEY_CHANGE_TOPIC);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Inspector Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean accessKeymodified = !mySettingsComponent.getAccessKey().equals(settings.getAccessKey());
        boolean secretKeymodified = !mySettingsComponent.getSecretKey().equals(settings.getSecretKey());
        return accessKeymodified | secretKeymodified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.setAccessKey(mySettingsComponent.getAccessKey());
        settings.setSecretKey(mySettingsComponent.getSecretKey());

        // Trigger all the subscriber of the API key notification so that they can change their behavior
        // accordingly.
        apiKeyChangeNotifier.afterAction(null);
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setAccessKey(settings.getAccessKey());
        mySettingsComponent.setSecretKey(settings.getSecretKey());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
