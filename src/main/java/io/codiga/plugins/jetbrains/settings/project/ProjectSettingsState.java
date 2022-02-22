package io.codiga.plugins.jetbrains.settings.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.codiga.plugins.jetbrains.Constants.INVALID_PROJECT_ID;


@State(
        name = "com.code_inspector.plugins.intellij.settings.project.ProjectSettingsState",
        storages = {@Storage("CodeInspectorProjectSettings.xml")}
)
public class ProjectSettingsState implements PersistentStateComponent<ProjectSettingsState> {

    public Boolean isProjectAssociated = false;
    public Boolean isEnabled = false;
    public Long projectId = INVALID_PROJECT_ID;

    public static ProjectSettingsState getInstance(Project p) {
        return p.getService(ProjectSettingsState.class);
    }

    public ProjectSettingsState(){
        super();
    }


    @Nullable
    @Override
    public ProjectSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
