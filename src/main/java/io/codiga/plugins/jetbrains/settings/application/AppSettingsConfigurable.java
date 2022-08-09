package io.codiga.plugins.jetbrains.settings.application;
import com.intellij.openapi.diagnostic.Logger;

import io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import io.codiga.plugins.jetbrains.topics.VisibilityKeyChangeNotifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.topics.ApiKeyChangeNotifier.CODIGA_API_KEY_CHANGE_TOPIC;
import static io.codiga.plugins.jetbrains.topics.VisibilityKeyChangeNotifier.CODIGA_VISIBILITY_CHANGE_TOPIC;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    final ApiKeyChangeNotifier apiKeyChangeNotifier =
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CODIGA_API_KEY_CHANGE_TOPIC);

    final VisibilityKeyChangeNotifier visibilityChangeNotifier =
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CODIGA_VISIBILITY_CHANGE_TOPIC);

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
        boolean completionModified = mySettingsComponent.useCompletion() != settings.getUseCompletion();
        boolean publicSnippetsModified = mySettingsComponent.usePublicSnippetsOnly() != settings.getPublicSnippetsOnly();
        boolean privateSnippetsModified = mySettingsComponent.usePrivateSnippetsOnly() != settings.getPrivateSnippetsOnly();
        boolean favoriteSnippetsModified = mySettingsComponent.useFavoriteSnippetsOnly() != settings.getFavoriteSnippetsOnly();

        LOGGER.debug("component public snippets: " + mySettingsComponent.usePublicSnippetsOnly());
        LOGGER.debug("component private snippets: " + mySettingsComponent.usePrivateSnippetsOnly());
        LOGGER.debug("componnent favorite snippets: " + mySettingsComponent.useFavoriteSnippetsOnly());

        LOGGER.debug("settings public snippets: " + settings.getPublicSnippetsOnly());
        LOGGER.debug("settings private snippets: " + settings.getPrivateSnippetsOnly());
        LOGGER.debug("settings favorite snippets: " + settings.getFavoriteSnippetsOnly());

        LOGGER.debug("publicSnippetsModified: " + publicSnippetsModified);
        LOGGER.debug("privateSnippetsModified: " + privateSnippetsModified);
        LOGGER.debug("favoriteSnippetsModified: " + favoriteSnippetsModified);


        return apiTokenModified || completionModified || publicSnippetsModified || privateSnippetsModified || favoriteSnippetsModified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.setApiToken(mySettingsComponent.getApiToken());
        settings.setUseCompletion(mySettingsComponent.useCompletion());
        settings.setPublicSnippetsOnly(mySettingsComponent.usePublicSnippetsOnly());
        settings.setPrivateSnippetsOnly(mySettingsComponent.usePrivateSnippetsOnly());
        settings.setFavoriteSnippetsOnly(mySettingsComponent.useFavoriteSnippetsOnly());
        // Trigger all the subscriber of the API key notification so that they can change their behavior
        // accordingly.
        apiKeyChangeNotifier.afterAction(null);
        visibilityChangeNotifier.afterAction(null);
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setApiToken(settings.getApiToken());
        mySettingsComponent.setUseEnabledCheckbox(settings.getUseCompletion());
        mySettingsComponent.setSnippetsVisiliblity(settings.getPrivateSnippetsOnly(), settings.getPublicSnippetsOnly(), settings.getFavoriteSnippetsOnly());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
