package com.code_inspector.plugins.intellij.settings.application;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the API parameters (access key and secret key) to access the
 * Code Inspector API. Used as an Application Service extension, to get
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
    private String fingerprint = "";

    public static AppSettingsState getInstance() {
        return ServiceManager.getService(AppSettingsState.class);
    }

    public String getFingerprint() {
        if(this.fingerprint == null || this.fingerprint.length() == 0) {
            this.fingerprint = RandomStringUtils.randomAlphanumeric(20);
        }
        return this.fingerprint;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setAccessKey(String s) {
        this.accessKey = s;
    }

    public void setSecretKey(String s) {
        this.secretKey = s;
    }

    public boolean hasApiKeys() {
        return getAccessKey().length() > 0 && getSecretKey().length() > 0;
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
