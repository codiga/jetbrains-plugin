package io.codiga.plugins.jetbrains.settings.application;
import com.intellij.openapi.diagnostic.Logger;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Represents the API parameters (access key and secret key) to access the
 * Codiga API. Used as an Application Service extension, to get
 * the instance, just use AppSettingsState.getInstance();
 */
@State(
        name = "com.code_inspector.plugins.intellij.settings.application.AppSettingsState",
        storages = {@Storage("CodeInspectorApplicationSettings.xml")}
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    @Tag
    private String accessKey = "";
    @Tag
    private String secretKey = "";
    @Tag
    private String apiToken = "";
    @Tag
    private String fingerprint = "";
    @Tag
    private Boolean useCompletion = true;
    @Tag
    private Boolean showDialogApiNotification = true;
    @Tag
    private Boolean showDialogOnboarding = true;
    @Tag
    private Boolean publicSnippetsOnly = false;
    @Tag
    private Boolean privateSnippetsOnly = false;
    @Tag
    private Boolean favoriteSnippetsOnly = false;

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    public String getFingerprint() {
        if(this.fingerprint == null || this.fingerprint.length() == 0) {
            this.fingerprint = RandomStringUtils.randomAlphanumeric(20);
        }
        return this.fingerprint;
    }

    public Boolean getUseCompletion() {
        return this.useCompletion;
    }

    public Boolean getShowDialogApiNotification() { return this.showDialogApiNotification; }

    public Boolean getShowDialogOnboarding() { return this.showDialogOnboarding; }

    public String getAccessKey() {
        return this.accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public String getApiToken() {
        return this.apiToken;
    }

    public boolean getPublicSnippetsOnly() { return this.publicSnippetsOnly; }

    public boolean getPrivateSnippetsOnly() { return this.privateSnippetsOnly; }

    public boolean getFavoriteSnippetsOnly() { return this.favoriteSnippetsOnly; }

    public void setApiToken(String s) {
        this.apiToken = s;
    }

    public void setUseCompletion(Boolean b) {
        this.useCompletion = b;
    }

    public boolean hasApiKeys() {
        return getAccessKey().length() > 0 && getSecretKey().length() > 0;
    }

    public boolean hasApiToken() {
        return getApiToken().length() > 0 ;
    }

    public void setShowDialogApiNotification(Boolean b) { this.showDialogApiNotification = b;}

    public void setShowDialogOnboarding(Boolean b) { this.showDialogOnboarding = b;}

    public void setPublicSnippetsOnly(Boolean b) {
        LOGGER.debug("[AppSettingsState] setPublicSnippetsOnly: " + b);
        this.publicSnippetsOnly = b;}

    public void setPrivateSnippetsOnly(Boolean b){
        LOGGER.debug("[AppSettingsState] setPrivateSnippetsOnly: " + b);
        this.privateSnippetsOnly = b;
    }

    public void setFavoriteSnippetsOnly(Boolean favoriteSnippetsOnly) {
        LOGGER.debug("[AppSettingsState] setFavoriteSnippetsOnly: " + favoriteSnippetsOnly);
        this.favoriteSnippetsOnly = favoriteSnippetsOnly;
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
