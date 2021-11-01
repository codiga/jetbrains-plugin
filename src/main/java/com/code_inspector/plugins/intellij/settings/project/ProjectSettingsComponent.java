package com.code_inspector.plugins.intellij.settings.project;

import com.code_inspector.api.GetProjectsQuery;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.topics.ApiKeyChangeNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static com.code_inspector.plugins.intellij.Constants.INVALID_PROJECT_ID;
import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.topics.ApiKeyChangeNotifier.CODE_INSPECTOR_API_KEY_CHANGE_TOPIC;
import static com.code_inspector.plugins.intellij.ui.UIConstants.*;

/**
 * Represent the project settings and what project to choose.
 */
public class ProjectSettingsComponent {

    private final JPanel myMainPanel = new JPanel();
    private ComboBox<GetProjectsQuery.Project> projectsCombo;
    private JCheckBox isProjectAssociatedCheckbox;
    private JCheckBox isEnabledCheckbox;
    private boolean isProjectAssociated;
    private boolean isEnabled;
    private List<GetProjectsQuery.Project> projectList;
    private IgnoredViolationsPanel ignoredViolationPanel;
    private final CodeInspectorApi codeInspectorApi = ApplicationManager.getApplication().getService(CodeInspectorApi.class);

    private Long selectedProjectId = INVALID_PROJECT_ID;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Build the UI for the project specific set up. If the API is working, get the list of project
     * and surface them in a combox box. If the API is not working, put a warning that tells
     * the user to configure the API keys.
     */
    public ProjectSettingsComponent() {
        final MessageBusConnection messageBusConnection =
            ApplicationManager.getApplication().getMessageBus().connect();
        buildInterface();

        /**
         * We subscribe to the topic when the API keys change in order to refresh the list of project we have.
         * When that is the case, we rebuild the content of the interface with the project we have
         * from the API.
         */
        messageBusConnection.subscribe(CODE_INSPECTOR_API_KEY_CHANGE_TOPIC, new ApiKeyChangeNotifier() {
            @Override
            public void beforeAction(Object context) { }

            @Override
            public void afterAction(Object context) {
                buildInterface();
            }
        });
    }

    /**
     * Generate the label to show for a project. If that is a project with a group, we prefix with the
     * group. If that is a project with a user, we prefix with the user.
     * @param project the project to map and create a label for.
     * @return the label to show in the preferences.
     */
    private String getProjectLabel(GetProjectsQuery.Project project) {
        if(project.group() != null) {
            return String.format("%s/%s", project.group().name(), project.name());
        }
        return String.format("%s/%s", project.owner().username(), project.name());
    }

    /**
     * Build the interface. This is a private method because the interface can be rebuilt (e.g. when the API key
     * changes) and therefore, this is convenient to have a method to just to this.
     */
    private void buildInterface() {
        myMainPanel.removeAll();
        myMainPanel.setLayout(new BorderLayout());
        JPanel projectSelectionPanel;
        ignoredViolationPanel = new IgnoredViolationsPanel(null);
        isProjectAssociatedCheckbox = new JCheckBox();

        isProjectAssociatedCheckbox.addActionListener( event -> {
            LOGGER.debug("setting project is associated to" + isEnabledCheckbox.isSelected());
            isProjectAssociated = isProjectAssociatedCheckbox.isSelected();
        });

        isEnabledCheckbox = new JCheckBox();

        isEnabledCheckbox.addActionListener(event -> {
            LOGGER.debug("setting isDisabled to" + isEnabledCheckbox.isSelected());
            isEnabled = isEnabledCheckbox.isSelected();
        });

        JButton configureButton = new JButton(SETTINGS_BUTTON_CONFIGURE_PROJECT);
        configureButton.addActionListener( event -> {
            try {
                String urlString = String.format("https://frontend.code-inspector.com/project/%s/preferences", selectedProjectId);
                Desktop.getDesktop().browse(new URL(urlString).toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (codeInspectorApi.isWorking()) {
            projectList = codeInspectorApi.getProjects();
            List<String> projectListString = projectList
                .stream()
                .map(this::getProjectLabel)
                .collect(Collectors.toList());

            this.projectsCombo = new ComboBox(projectListString.toArray());

            this.projectsCombo.addActionListener(
                arg -> {
                    int idx = this.projectsCombo.getSelectedIndex();
                    GetProjectsQuery.Project selectedProject = projectList.get(idx);
                    Long selectedProjectId = ((BigDecimal)selectedProject.id()).longValue();
                    setSelectedProjectId(selectedProjectId);
                }
            );

            projectSelectionPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(SETTINGS_PROJECT_ENABLE), this.isEnabledCheckbox, 0, false)
                .addVerticalGap(5)
                .addSeparator(1)
                .addVerticalGap(5)
                .addLabeledComponent(new JBLabel(SETTINGS_ASSOCIATE_CODE_INSPECTOR_PROJECT), this.isProjectAssociatedCheckbox, 0, false)
                .addVerticalGap(5)
                .addLabeledComponent(new JBLabel(SETTINGS_CHOOSE_PROJECT), this.projectsCombo, 0, false)
                .addVerticalGap(5)
                .addSeparator(1)
                .addVerticalGap(5)
                .addLabeledComponent(new JBLabel(SETTINGS_LABEL_REMOVE_IGNORED_VIOLATION), ignoredViolationPanel, 0, true)
                .addVerticalGap(5)
                .addSeparator(1)
                .addVerticalGap(5)
                .addLabeledComponent(new JBLabel(SETTINGS_LABEL_CONFIGURE_PROJECT), configureButton, 0, false)
                .addComponentFillVertically(new JPanel(), 1)
                .getPanel();
        } else {
            projectSelectionPanel = FormBuilder.createFormBuilder()
                .addComponent(new JBLabel(SETTINGS_API_NOT_WORKING), 1)
                .addComponentFillVertically(new JPanel(), 1)
                .getPanel();
        }


        myMainPanel.add(projectSelectionPanel, BorderLayout.LINE_START);
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return projectsCombo;
    }

    public Long getSelectedProjectId() {
        return this.selectedProjectId;
    }

    public Boolean isProjectAssociated() {
        return this.isProjectAssociated;
    }

    public Boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Set the selected project in the UI
     * @param projectIdentifier the identifier of the project on Code Inspector
     */
    public void setSelectedProjectId(Long projectIdentifier) {
        GetProjectsQuery.Project selectedProject = null;
        this.selectedProjectId = projectIdentifier;
        int idx = 0;

        if (this.projectList == null) {
            return;
        }

        // Loop through the project list to see which one has the identifier we are looking for
        for(int i = 0 ; i < this.projectList.size() ; i++) {
            GetProjectsQuery.Project p = this.projectList.get(i);
            Long projectId = ((BigDecimal)p.id()).longValue();
            if (projectId.equals(projectIdentifier)) {
                idx = i;
                selectedProject = p;
            }
        }

        // if we found the project, set the project in the list.
        if (idx != 0) {
            ignoredViolationPanel.updateModel(selectedProject);
            ignoredViolationPanel.repaint();
            this.projectsCombo.setSelectedIndex(idx);
        }
    }


    /**
     * Activate the enabled checkbox. Used by the configurable to set the project status.
     * @param b - should we enable for this project or not
     */
    public void setIsProjectAssociatedCheckbox(Boolean b) {
        if (this.isProjectAssociatedCheckbox != null) {
            this.isProjectAssociated = b;
            this.isProjectAssociatedCheckbox.setSelected(b);
        }
    }

    /**
     * Set the status of the disabled checkbox
     * @param b
     */
    public void setIsEnabledCheckbox(Boolean b) {
        if (this.isEnabledCheckbox != null) {
            this.isEnabled = b;
            this.isEnabledCheckbox.setSelected(b);
        }
    }
}

