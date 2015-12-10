package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides access to worklow scheme migration tasks.
 *
 * @since v5.2
 */
public interface WorkflowSchemeMigrationTaskAccessor
{
    /**
     * Returns the currently active task associated with the given project,
     * or null if there are now such tasks.
     *
     * @param project project who's tasks will be sought
     * @return currently active task associated with the given project,
     *          or null if there are now such tasks.
     */
    @Nullable
    TaskDescriptor<WorkflowMigrationResult> getActive(@Nonnull Project project);

    /**
     * Returns the currently active task that is migrating any of the projects associated with the passed draft scheme.
     *
     * @param scheme the scheme.
     * @param onlyDraftMigrations if true, then only draft migration will be considered.
     * @return the task performing the migration, or null if there are now such tasks.
     */
    @Nullable
    TaskDescriptor<WorkflowMigrationResult> getActiveByProjects(@Nonnull DraftWorkflowScheme scheme, boolean onlyDraftMigrations);

    /**
     * Returns the currently active task that is migrating any of the projects associated with the passed draft scheme.
     *
     * @param scheme the scheme.
     * @param onlyDraftMigrations if true, then only draft migration will be considered.
     * @param onlyNonSafeToDelete if true, then only migrations which are not marked as safe for deletion will be considered (see com.atlassian.jira.workflow.migration.EnterpriseWorkflowTaskContext#isSafeToDelete).
     * @return the task performing the migration, or null if there are now such tasks.
     */
    @Nullable
    TaskDescriptor<WorkflowMigrationResult> getActiveByProjects(@Nonnull DraftWorkflowScheme scheme, boolean onlyDraftMigrations, boolean onlyNonSafeToDelete);

    /**
     * Returns the currently active task that is migrating the passed scheme.
     *
     * @param scheme the scheme.
     * @return the task performing the migration, or null if there are now such tasks.
     */
    @Nullable
    TaskDescriptor<WorkflowMigrationResult> getActive(@Nonnull WorkflowScheme scheme);
}
