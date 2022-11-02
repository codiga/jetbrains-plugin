package io.codiga.plugins.jetbrains.rosie;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores configuration related to the Codiga config file.
 */
@State(
    name = "io.codiga.plugins.jetbrains.rosie.CodigaConfigState",
    storages = {@Storage("CodigaConfigSettings.xml")}
)
@Service(Service.Level.PROJECT)
public final class CodigaConfigState implements PersistentStateComponent<CodigaConfigState> {

    /**
     * Indicates whether to show a notification popup for the current project, regarding a missing codiga.yml file.
     *
     * @see io.codiga.plugins.jetbrains.starter.RosieStartupActivity#showConfigureDefaultConfigFileNotification(Project)
     */
    @Getter
    @Setter
    public boolean shouldNotifyUserToCreateCodigaConfig = true;

    public static CodigaConfigState getInstance(Project project) {
        return project.getService(CodigaConfigState.class);
    }

    @Nullable
    @Override
    public CodigaConfigState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CodigaConfigState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
