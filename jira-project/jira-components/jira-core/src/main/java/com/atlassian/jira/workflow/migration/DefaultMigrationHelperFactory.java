package com.atlassian.jira.workflow.migration;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Preconditions;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

/**
 * @since v5.1
 */
public class DefaultMigrationHelperFactory implements MigrationHelperFactory
{
    private final WorkflowManager workflowManager;
    private final OfBizDelegator delegator;
    private final SchemeManager schemeManager;
    private final JiraAuthenticationContext authCtx;
    private final ConstantsManager constantsManager;
    private final TaskManager taskManager;
    private final IssueIndexManager issueIndexManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final EventPublisher eventPublisher;

    public DefaultMigrationHelperFactory(WorkflowManager workflowManager, OfBizDelegator delegator,
            WorkflowSchemeManager schemeManager, ConstantsManager constantsManager,
            JiraAuthenticationContext authCtx, TaskManager taskManager, IssueIndexManager issueIndexManager,
            WorkflowSchemeManager workflowSchemeManager, EventPublisher eventPublisher)
    {
        this.workflowManager = workflowManager;
        this.delegator = delegator;
        this.schemeManager = schemeManager;
        this.authCtx = authCtx;
        this.constantsManager = constantsManager;
        this.taskManager = taskManager;
        this.issueIndexManager = issueIndexManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AssignableWorkflowSchemeMigrationHelper createMigrationHelper(Project project, AssignableWorkflowScheme scheme)
            throws GenericEntityException
    {
        Preconditions.checkNotNull(project, "project cannot be null.");

        return new AssignableWorkflowSchemeMigrationHelper(project, scheme, workflowManager, delegator,
                schemeManager, authCtx.getI18nHelper(), authCtx.getLoggedInUser(), constantsManager,
                taskManager, issueIndexManager, workflowSchemeManager, eventPublisher);
    }

    @Override
    public DraftWorkflowSchemeMigrationHelper createMigrationHelper(Project triggerProject, List<Project> projects, DraftWorkflowScheme draft)
            throws GenericEntityException
    {
        Preconditions.checkNotNull(projects, "projects cannot be null.");

        return new DraftWorkflowSchemeMigrationHelper(triggerProject, projects, draft, workflowManager, delegator,
                schemeManager, authCtx.getI18nHelper(), authCtx.getLoggedInUser(), constantsManager,
                taskManager, issueIndexManager, workflowSchemeManager, eventPublisher);
    }
}
