package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @since v5.2
 */
public class DefaultWorkflowSchemeMigrationTaskAccessor implements WorkflowSchemeMigrationTaskAccessor
{
    private final TaskManager taskManager;
    private final WorkflowSchemeManager manager;

    public DefaultWorkflowSchemeMigrationTaskAccessor(TaskManager taskManager, WorkflowSchemeManager manager)
    {
        this.taskManager = taskManager;
        this.manager = manager;
    }

    @Override
    public TaskDescriptor<WorkflowMigrationResult> getActive(@Nonnull Project project)
    {
        return taskManager.getLiveTask(new EnterpriseWorkflowTaskContext(project));
    }

    @Override
    public TaskDescriptor<WorkflowMigrationResult> getActiveByProjects(@Nonnull DraftWorkflowScheme scheme, boolean onlyDraftMigrations)
    {
        return getActiveByProjects(scheme, onlyDraftMigrations, false);
    }

    @Override
    public TaskDescriptor<WorkflowMigrationResult> getActiveByProjects(@Nonnull DraftWorkflowScheme scheme, boolean onlyDraftMigrations, boolean onlyNonSafeToDelete)
    {
        List<Project> projectsUsing = manager.getProjectsUsing(scheme.getParentScheme());
        if (projectsUsing.isEmpty())
        {
            return null;
        }

        Project primaryProject = projectsUsing.get(0);

        TaskDescriptor<WorkflowMigrationResult> task = findSchemeMigrationTask(new ProjectMigrationTaskMatcher(scheme, projectsUsing,
                primaryProject, onlyDraftMigrations, onlyNonSafeToDelete));

        if (task == null)
        {
            // Check parent scheme migrations.
            task = getActive(scheme.getParentScheme());
        }

        return task;
    }

    @Override
    public TaskDescriptor<WorkflowMigrationResult> getActive(@Nonnull WorkflowScheme scheme)
    {
        return findSchemeMigrationTask(new SchemeMigrationTaskMatcher(scheme));
    }

    @SuppressWarnings("unchecked")
    public TaskDescriptor<WorkflowMigrationResult> findSchemeMigrationTask(AbstractSchemeMigrationTaskMatcher taskMatcher)
    {
        return (TaskDescriptor<WorkflowMigrationResult>) taskManager.findFirstTask(taskMatcher);
    }

    private static abstract class AbstractSchemeMigrationTaskMatcher implements TaskMatcher
    {
        protected final WorkflowScheme scheme;

        AbstractSchemeMigrationTaskMatcher(WorkflowScheme scheme)
        {
            this.scheme = scheme;
        }

        @Override
        public boolean match(TaskDescriptor<?> descriptor)
        {
            if (descriptor.isFinished())
            {
                return false;
            }

            if (!(descriptor.getTaskContext() instanceof EnterpriseWorkflowTaskContext))
            {
                return false;
            }

            EnterpriseWorkflowTaskContext that = (EnterpriseWorkflowTaskContext) descriptor.getTaskContext();

            return match(that);
        }

        abstract boolean match(EnterpriseWorkflowTaskContext that);
    }

    static class SchemeMigrationTaskMatcher extends AbstractSchemeMigrationTaskMatcher
    {
        SchemeMigrationTaskMatcher(WorkflowScheme scheme)
        {
            super(scheme);
        }

        @Override
        boolean match(EnterpriseWorkflowTaskContext that)
        {
            return scheme.getId().equals(that.getSchemeId()) && scheme.isDraft() == that.isDraftMigration();
        }
    }

    static class ProjectMigrationTaskMatcher extends AbstractSchemeMigrationTaskMatcher
    {
        private final EnterpriseWorkflowTaskContext matchContext;
        private final boolean onlyDraftMigrations;
        private final boolean onlyNonSafeToDelete;

        ProjectMigrationTaskMatcher(WorkflowScheme scheme, List<Project> projectsUsing, Project primaryProject, boolean onlyDraftMigrations, boolean onlyNonSafeToDelete)
        {
            super(scheme);
            this.onlyDraftMigrations = onlyDraftMigrations;
            this.onlyNonSafeToDelete = onlyNonSafeToDelete;
            matchContext = new EnterpriseWorkflowTaskContext(primaryProject, projectsUsing, scheme.getId(), true);
        }

        @Override
        boolean match(EnterpriseWorkflowTaskContext that)
        {
            return matchContext.equals(that)
                    && (!onlyDraftMigrations || that.isDraftMigration())
                    && (!onlyNonSafeToDelete || !that.isSafeToDelete());
        }
    }
}
