package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

/**
 * Provides a way to create instances of {@link WorkflowSchemeMigrationHelper}.
 * @since v5.1
 */
public interface MigrationHelperFactory
{
    /**
     * Create an instance of a {@link AssignableWorkflowSchemeMigrationHelper}.
     *
     * @param project the project to migration. Can't be null.
     * @param scheme the scheme to migrate to. Can't be null.
     * @return the created helper.
     * @throws GenericEntityException if DB errors.
     */
    AssignableWorkflowSchemeMigrationHelper createMigrationHelper(Project project, AssignableWorkflowScheme scheme)
            throws GenericEntityException;

    /**
     * Create an instance of a {@link DraftWorkflowSchemeMigrationHelper}.
     *
     *
     * @param triggerProject
     * @param projects list of projects to migrate. Can't be null.
     * @param draft the draft scheme to migrate to. Can't be null.
     * @return the created helper.
     * @throws GenericEntityException if DB errors.
     */
    DraftWorkflowSchemeMigrationHelper createMigrationHelper(Project triggerProject, List<Project> projects, DraftWorkflowScheme draft)
            throws GenericEntityException;
}
