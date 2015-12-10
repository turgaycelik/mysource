package com.atlassian.jira.issue.operation;

import com.atlassian.jira.bulkedit.operation.BulkOperationException;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class BulkTransitionIssueOperation implements ProgressAwareBulkOperation, WorkflowIssueOperation
{
    private final ProgressAwareBulkOperation operation;
    private final ActionDescriptor actionDescriptor;

    public BulkTransitionIssueOperation(final ProgressAwareBulkOperation operation,
                                        final ActionDescriptor actionDescriptor)
    {
        this.operation = operation;
        this.actionDescriptor = actionDescriptor;
    }

    @Override
    public ActionDescriptor getActionDescriptor()
    {
        return actionDescriptor;
    }

    @Override
    public String getNameKey()
    {
        return operation.getNameKey();
    }

    @Override
    public String getDescriptionKey()
    {
        return operation.getDescriptionKey();
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser)
    {
        return operation.canPerform(bulkEditBean, ApplicationUsers.from(applicationUser.getDirectoryUser()));
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser,
                        final Context taskContext) throws BulkOperationException
    {
        operation.perform(bulkEditBean, ApplicationUsers.from(applicationUser.getDirectoryUser()), taskContext);
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean bulkEditBean)
    {
        return operation.getNumberOfTasks(bulkEditBean);
    }

    @Override
    public String getOperationName()
    {
        return operation.getOperationName();
    }

    @Override
    public String getCannotPerformMessageKey()
    {
        return operation.getCannotPerformMessageKey();
    }
}
