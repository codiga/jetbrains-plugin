package io.codiga.plugins.jetbrains.settings.project;

import io.codiga.api.GetProjectsQuery;
import io.codiga.plugins.jetbrains.cache.AnalysisDataCache;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.FileContentUtil;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Optional;

import static io.codiga.plugins.jetbrains.ui.UIConstants.SETTINGS_BUTTON_REMOVE_IGNORED_VIOLATION;

/**
 * Implements a panel that is included in the project preferences. This panel lists all
 * the ignored violation and has a button to remove ignored violations.
 */
public class IgnoredViolationsPanel extends JPanel {

    private final JBTable table;
    private GetProjectsQuery.Project currentProject;

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    /**
     * Build the panel
     * @param project - the project (retrieved from GetProjects GraphQL API) that contains the ignored violations.
     */
    public IgnoredViolationsPanel(GetProjectsQuery.Project project) {
        super();
        table = new JBTable(new IgnoredViolationsTableModel(project));
        final JButton removeButton = new JButton(SETTINGS_BUTTON_REMOVE_IGNORED_VIOLATION);
        currentProject = project;

        // button to remove a violation.
        removeButton.addActionListener(e -> {
            if (currentProject == null){
                return;
            }

            Long projectId = ((BigDecimal)currentProject.id()).longValue();
            int selectedRow = table.getSelectedRow();
            GetProjectsQuery.ViolationsToIgnore violationToIgnore = currentProject.violationsToIgnore().get(selectedRow);

            // remove the violation via the API
            codigaApi.removeViolationToIgnore(projectId, violationToIgnore.rule(), violationToIgnore.tool(),
                violationToIgnore.language(), Optional.ofNullable(violationToIgnore.filename()), Optional.empty());

            // refetch the list of violations and update the model
            updateModelInternal(projectId);

            // make sure we refresh our cache so that violations are coming back.
            AnalysisDataCache.getInstance().invalidateCache();

            // re-parse files to surface issues that are now no longer being ignored.
            FileContentUtil.reparseOpenedFiles();
        });

        JBScrollPane scrollPane = new JBScrollPane(table);
        table.setFillsViewportHeight(true);

        scrollPane.setPreferredSize(new Dimension(600, 400));
        this.add(scrollPane);
        this.add(removeButton);
        this.setPreferredSize(new Dimension(600, 500));
    }

    /**
     * Refresh the model given a project id. It retrieves the list of project again
     * and update the model. This is a private method that calls the API to refresh
     * the data and update the data model for the table.
     *
     * @param projectId - the identifier of the project on Codiga (a long number)
     */
    private void updateModelInternal(Long projectId) {
        // refetch the list of violations and update the model
        java.util.List<GetProjectsQuery.Project> projectList = codigaApi.getProjects();

        for (GetProjectsQuery.Project p: projectList) {
            Long i = ((BigDecimal)p.id()).longValue();
            if(i.equals(projectId)){
                TableModel newModel = new IgnoredViolationsTableModel(p);
                table.setModel(newModel);
                table.repaint();
            }
        }
    }

    /**
     * Public method to update the model.
     * @param project - the project to use to refresh the data model.
     */
    public void updateModel(GetProjectsQuery.Project project) {
        currentProject = project;
        Long projectId = ((BigDecimal)project.id()).longValue();
        updateModelInternal(projectId);
    }
}
