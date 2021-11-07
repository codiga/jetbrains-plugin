package io.codiga.plugins.jetbrains.settings.application;

import io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier.CODIGA_API_KEY_CHANGE_TOPIC;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    final ApiKeyChangeNotifier apiKeyChangeNotifier =
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CODIGA_API_KEY_CHANGE_TOPIC);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Codiga Settings";
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
        boolean apiTokenModified = !mySettingsComponent.getApiToken().equals(settings.getApiToken());
        return apiTokenModified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.setApiToken(mySettingsComponent.getApiToken());

        // Trigger all the subscriber of the API key notification so that they can change their behavior
        // accordingly.
        apiKeyChangeNotifier.afterAction(null);
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setApiToken(settings.getApiToken());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
