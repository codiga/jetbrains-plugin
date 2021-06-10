package com.code_inspector.plugins.intellij.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.RowIcon;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Consumer;
import com.intellij.icons.AllIcons;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;

public class CodeInspectorStatusBar implements StatusBarWidgetFactory {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    public static class CodeInspectorStatusBarWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {

        private static final float fontToScale = JBUIScale.scale(12f);

        private static Icon scaleIcon(Icon sourceIcon) {
            return IconUtil.scaleByFont(sourceIcon, null, fontToScale - 2);
        }
        private static final Icon errorGray = scaleIcon(AllIcons.Nodes.WarningIntroduction);
        private static final Icon errorColor = scaleIcon(AllIcons.General.Error);
        private static final Icon warningGray = scaleIcon(AllIcons.General.ShowWarning);
        private static final Icon warningColor = scaleIcon(AllIcons.General.Warning);
        private static final Icon infoGray = scaleIcon(AllIcons.General.Note);
        private static final Icon infoColor = scaleIcon(AllIcons.General.Information);

        public static final Icon EMPTY_EWI_ICON = warningColor;
        private final Project project;
        private StatusBar myStatusBar;
        private Icon myCurrentIcon = EMPTY_EWI_ICON;
        private String myToolTipText = "CodeInspector";

        public CodeInspectorStatusBarWidget(@NotNull Project project) {
            this.project = project;
            update();
        }

        @Override
        public WidgetPresentation getPresentation(){
            return this;
        }

        private void update() {
            myCurrentIcon = EMPTY_EWI_ICON;
            myToolTipText = "blabla";
        }

        @NotNull
        @Override
        public String ID() {
            return "CodeInspectorAnalysisStatus";
        }


        @Override
        public void install(@NotNull StatusBar statusBar) {
            myStatusBar = statusBar;
        }

        @Override
        public void dispose() {}



        @Nullable
        @Override
        public String getTooltipText() {
            return myToolTipText;
        }

        @Nullable
        @Override
        public Consumer<MouseEvent> getClickConsumer() {
            return mouseEvent -> {
                ToolWindow myToolWindow = ToolWindowManager.getInstance(project).getToolWindow("DeepCode");
                myToolWindow.show(null);
            };
        }

        @Override
        public @Nullable Icon getIcon() {
            return myCurrentIcon;
        }
    }

    @Override
    public @NonNls
    @NotNull String getId() {
        return "code-inspector-widget";
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Code Inspector";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        LOGGER.debug("Creating status bar");
        return new CodeInspectorStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {

    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
