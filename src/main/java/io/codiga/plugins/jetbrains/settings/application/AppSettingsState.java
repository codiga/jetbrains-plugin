package io.codiga.plugins.jetbrains.settings.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public String getAccessKey() {
        return this.accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public String getApiToken() {
        return this.apiToken;
    }

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
