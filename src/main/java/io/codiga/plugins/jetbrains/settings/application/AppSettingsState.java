package io.codiga.plugins.jetbrains.settings.application;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.codiga.plugins.jetbrains.Constants.*;

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
    public static final String INVALID_RECORD_ERROR_MESSAGE = "find: An invalid record was encountered. (-67701)";

    // See https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html#retrieve-stored-credentials
    CredentialAttributes credentialAttributes = createCredentialAttributes(CREDENTIALS_KEY);
    @Tag
    private String fingerprint = "";
    @Tag
    private Boolean useCompletion = true;
    @Tag
    private Boolean useInlineCompletion = true;
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
    @Tag
    private Boolean codigaEnabled = true;

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
            CredentialAttributesKt.generateServiceName(CREDENTIALS_SERVICE, key)
        );
    }

    public String getFingerprint() {
        if (this.fingerprint == null || this.fingerprint.length() == 0) {
            this.fingerprint = RandomStringUtils.randomAlphanumeric(20);
        }
        return this.fingerprint;
    }

    public Boolean getUseCompletion() {
        return this.useCompletion;
    }

    public void setUseCompletion(Boolean b) {
        this.useCompletion = b;
    }

    public Boolean getShowDialogApiNotification() {
        return this.showDialogApiNotification;
    }

    public void setShowDialogApiNotification(Boolean b) {
        this.showDialogApiNotification = b;
    }

    public Boolean getShowDialogOnboarding() {
        return this.showDialogOnboarding;
    }

    public void setShowDialogOnboarding(Boolean b) {
        this.showDialogOnboarding = b;
    }


    public String getApiToken() {
        Credentials credentials = null;
        try {
            credentials = PasswordSafe.getInstance().get(credentialAttributes);
        } catch (Throwable e) {
            //Prevents an issue related to macOS keychain credentials store to be surfaced to users,
            // but still logging it to not lose sight of it.
            // See https://github.com/codiga/jetbrains-plugin/issues/151
            if (e.getMessage().contains(INVALID_RECORD_ERROR_MESSAGE)) {
                LOGGER.debug("[AppSettingsState] macOS credentials store: An invalid record was encountered.");
            }
        }
        if (credentials != null) {
            return credentials.getPasswordAsString();
        }
        return null;
    }

    public void setApiToken(String s) {
        Credentials credentials = new Credentials(CREDENTIALS_KEY, s);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public boolean getPublicSnippetsOnly() {
        return this.publicSnippetsOnly;
    }

    public void setPublicSnippetsOnly(Boolean b) {
        LOGGER.debug("[AppSettingsState] setPublicSnippetsOnly: " + b);
        this.publicSnippetsOnly = b;
    }

    public boolean getPrivateSnippetsOnly() {
        return this.privateSnippetsOnly;
    }

    public void setPrivateSnippetsOnly(Boolean b) {
        LOGGER.debug("[AppSettingsState] setPrivateSnippetsOnly: " + b);
        this.privateSnippetsOnly = b;
    }

    public boolean getCodigaEnabled() {
        return this.codigaEnabled;
    }

    public void setCodigaEnabled(Boolean b) {
        this.codigaEnabled = b;
    }

    public boolean getFavoriteSnippetsOnly() {
        return this.favoriteSnippetsOnly;
    }

    public void setFavoriteSnippetsOnly(Boolean favoriteSnippetsOnly) {
        LOGGER.debug("[AppSettingsState] setFavoriteSnippetsOnly: " + favoriteSnippetsOnly);
        this.favoriteSnippetsOnly = favoriteSnippetsOnly;
    }

    public boolean getUseInlineCompletion() {
        return this.useInlineCompletion;
    }

    public void setUseInlineCompletion(Boolean b) {
        LOGGER.debug("[AppSettingsState] useInlineCompletion: " + b);
        this.useInlineCompletion = b;
    }


    public boolean hasApiToken() {
        return getApiToken().length() > 0;
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
