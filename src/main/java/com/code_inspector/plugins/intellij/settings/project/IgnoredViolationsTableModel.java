package com.code_inspector.plugins.intellij.settings.project;

import com.code_inspector.api.GetProjectsQuery;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Implement a TableModel for the ignored violations in the preferences.
 * This model takes a project and display all the required data for the table,
 * taking its ignored violations. The project is retrieved using
 * the GetProjects query.
 */
public class IgnoredViolationsTableModel implements TableModel {
    private static String[] columnNames = new String[]{"Scope", "Filename or Prefix", "Rule", "Tool"};
    private final GetProjectsQuery.Project project;

    public IgnoredViolationsTableModel(GetProjectsQuery.Project _project) {
        project = _project;
    }

    @Override
    public int getRowCount() {
        if (project != null) {
            return project.violationsToIgnore().size();
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    /**
     * We only report string
     * @param columnIndex - ignore
     * @return - String.class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * No cell is editable.
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Get the value in the table at a specific row and column. The row indicates the ignored violation.
     * For the column, we use the following
     *  - column 1 indicates the type of ignored violation (project-wide, file-wide, prefix-wide)
     *  - column 2 reports the Filename or Prefix
     *  - column 3 reports the rule being ignored
     *  - column 4 reports the tool that processes the ignored violation.
     * @param rowIndex the position in the array (e.g. the ignored violation)
     * @param columnIndex the value we are trying to get.
     * @return the value to show (we always return a String)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GetProjectsQuery.ViolationsToIgnore violationToIgnore =  project.violationsToIgnore().get(rowIndex);
        switch (columnIndex) {
            case 0: {
                if (violationToIgnore.prefix() == null && violationToIgnore.filename() == null) {
                    return "Project";
                }
                if (violationToIgnore.prefix() == null && violationToIgnore.filename() != null) {
                    return "File";
                }
                if (violationToIgnore.prefix() != null && violationToIgnore.filename() == null) {
                    return "Prefix";
                }
                return "Unknown";
            }
            case 1: {
                if (violationToIgnore.filename() != null) {
                    return violationToIgnore.filename();
                }
                if (violationToIgnore.prefix() != null) {
                    return violationToIgnore.prefix();
                }
                return "N/A";
            }
            case 2: {
                return violationToIgnore.rule();
            }
            case 3: {
                return violationToIgnore.tool();
            }
        }
        return "N/A";
    }

    /**
     * This table cannot be changed.
     * @param aValue - N/A
     * @param rowIndex  - N/A
     * @param columnIndex  - N/A
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { }

    /**
     * Add a listener - this is not used.
     * @param l - not used
     */
    @Override
    public void addTableModelListener(TableModelListener l) { }

    /**
     * Remove a listener - this is not used.
     * @param l - not used
     */
    @Override
    public void removeTableModelListener(TableModelListener l) { }
}
