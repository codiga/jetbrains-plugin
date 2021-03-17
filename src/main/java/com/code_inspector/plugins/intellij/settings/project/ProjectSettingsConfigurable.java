package com.code_inspector.plugins.intellij.settings.project;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

public class ProjectSettingsConfigurable implements Configurable {
    private ProjectSettingsComponent mySettingsComponent;
    private final Project project;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Inspector Project Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    public ProjectSettingsConfigurable(Project p){
        this.project = p;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new ProjectSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        if (!this.mySettingsComponent.getSelectedProjectId().equals(settings.projectId)) {
            return true;
        }
        return (!this.mySettingsComponent.isEnabled().equals(settings.isEnabled));
    }

    @Override
    public void apply() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        settings.projectId = this.mySettingsComponent.getSelectedProjectId();
        settings.isEnabled = this.mySettingsComponent.isEnabled();
    }

    @Override
    public void reset() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(project);
        this.mySettingsComponent.setSelectedProjectId(settings.projectId);
        this.mySettingsComponent.setIsEnabled(settings.isEnabled);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
