package com.atlassian.jira.permission;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.WorkflowIssueOperation;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class DefaultPermissionContextFactory implements PermissionContextFactory
{
    private static final Logger log = Logger.getLogger(DefaultPermissionContextFactory.class);

    private final WorkflowManager workflowManager;

    public DefaultPermissionContextFactory(WorkflowManager workflowManager)
    {
        this.workflowManager = workflowManager;
    }

    public PermissionContext getPermissionContext(Issue issue)
    {
        return new PermissionContextImpl(issue, issue.getProjectObject(), issue.getStatusObject());
    }

    public PermissionContext getPermissionContext(Project project)
    {
        return new PermissionContextImpl(null, project, null);
    }

    public PermissionContext getPermissionContext(Issue issue, Status issueStatus)
    {
        return new PermissionContextImpl(issue, issue.getProjectObject(), issueStatus);
    }

    public PermissionContext getPermissionContext(GenericValue projectOrIssue)
    {
        if ("Issue".equals(projectOrIssue.getEntityName()))
        {
            IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
            Issue issue = issueFactory.getIssue(projectOrIssue);
            Project project = issue.getProjectObject();
            Status status = issue.getStatusObject();
            return new PermissionContextImpl(issue, project, status);
        }
        else if ("Project".equals(projectOrIssue.getEntityName()))
        {
            Project project = ComponentAccessor.getProjectFactory().getProject(projectOrIssue);
            return new PermissionContextImpl(null, project, null);
        }
        else throw new IllegalArgumentException("DefaultPermissionContextFactory passed a "+projectOrIssue.getClass().getName()+": can only accept an Issue or Project");
    }

    @Override
    public PermissionContext getPermissionContext(Issue issue, ActionDescriptor actionDescriptor)
    {
        if (actionDescriptor == null)
        {
            return getPermissionContext(issue);
        }

        ResultDescriptor unconditionalResult = actionDescriptor.getUnconditionalResult();
        if (unconditionalResult.getStep() == 0 && unconditionalResult.getJoin() != 0)
        {
            // join support is still experimental. If we encounter a join we cannot know the destination status (JT)
            log.warn("Encountered join "+unconditionalResult.getJoin()+" in result "+actionDescriptor.getName()+"; using default issue permissions");
            return getPermissionContext(issue);
        }
        int newStepId = unconditionalResult.getStep();
        JiraWorkflow workflow;
        try
        {
            workflow = workflowManager.getWorkflow(issue);

            //JRA-12017: Need to allow -1 for a step, which means no transition to another step will occur, so we
            //can simply return with a permissioncontext that uses the existing status.
            if(newStepId == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
            {
                return getPermissionContext(issue);
            }
            else
            {
                Status status = workflow.getLinkedStatusObject(workflow.getDescriptor().getStep(newStepId));
                if (status == null)
                {
                    throw new RuntimeException("No status associated with destination step " + newStepId + " for " + issue + " in workflow " + workflow.getName());
                }
                return getPermissionContext(issue, status);
            }

        }
        catch (WorkflowException e)
        {
            log.error("Could not find workflow associated with issue " + issue, e);
            throw new RuntimeException("Could not find workflow associated with issue " + issue);
        }
    }

    /**
     * Checks if we're in a workflow operation, and if so constructs a PermissionContext with the destination status.
     * Otherwise returns a normal PC wrapping the issue.
     */
    public PermissionContext getPermissionContext(OperationContext operationContext, Issue issue)
    {
        IssueOperation issueOperation = operationContext.getIssueOperation();
        if (issueOperation instanceof WorkflowIssueOperation)
        {
            ActionDescriptor actionDescriptor = ((WorkflowIssueOperation) issueOperation).getActionDescriptor();
            return getPermissionContext(issue, actionDescriptor);
        }

        return getPermissionContext(issue);
    }

}
