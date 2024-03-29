package io.codiga.plugins.jetbrains.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import icons.CodigaIcons;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import io.codiga.plugins.jetbrains.topics.CodigaEnabledStatusNotifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static io.codiga.plugins.jetbrains.topics.CodigaEnabledStatusNotifier.CODIGA_ENABLED_CHANGE_TOPIC;


public class StatusBar implements StatusBarWidgetFactory {
    private static final String ID = "codiga.status";
    private final AppSettingsState settings = AppSettingsState.getInstance();


    @Override
    public @NonNls @NotNull String getId() {
        return ID;
    }

    @Nls
    @Override
    public @NotNull String getDisplayName() {
        return "Codiga";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        com.intellij.openapi.wm.StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            ApplicationManager.getApplication()
                .getMessageBus().connect()
                .subscribe(CodigaEnabledStatusNotifier.CODIGA_ENABLED_CHANGE_TOPIC,
                    (CodigaEnabledStatusNotifier) context -> statusBar.updateWidget(getId()));
        }
        return new CodigaStatusWidget();
    }


    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // no need
    }

    @Override
    public boolean canBeEnabledOn(@NotNull com.intellij.openapi.wm.StatusBar statusBar) {
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    private static class CodigaStatusWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {
        private AppSettingsState settings = AppSettingsState.getInstance();

        @Override
        public @NotNull String ID() {
            return ID;
        }

        @Override
        public void install(@NotNull com.intellij.openapi.wm.StatusBar statusBar) {
            // no need
        }

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @Nullable String getTooltipText() {
            if (settings.getCodigaEnabled()) {
                return "Codiga completion is enabled";
            } else {
                return "Codiga completion is disabled";
            }

        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return __ -> {
                if (settings.getCodigaEnabled()) {
                    settings.setCodigaEnabled(false);
                } else {
                    settings.setCodigaEnabled(true);
                }
                ApplicationManager.getApplication().getMessageBus().syncPublisher(CODIGA_ENABLED_CHANGE_TOPIC).afterAction(null);
            };
        }

        @Override
        public @Nullable Icon getIcon() {
            if (settings.getCodigaEnabled()) {
                return CodigaIcons.Codiga_enabled_icon;
            } else {
                return CodigaIcons.Codiga_disabled_icon;
            }
        }

        @Override
        public void dispose() {
            // no need
        }
    }
}
