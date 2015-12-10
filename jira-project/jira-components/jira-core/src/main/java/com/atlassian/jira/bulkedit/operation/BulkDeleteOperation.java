package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.log4j.Logger;

import java.util.List;

public class BulkDeleteOperation implements ProgressAwareBulkOperation
{
    protected static final Logger log = Logger.getLogger(BulkDeleteOperation.class);

    public static final String NAME = "BulkDelete";
    public static final String NAME_KEY = "bulk.delete.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.delete.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.delete.cannotperform";

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser)
    {
        // Check whether the user has the delete permission for all the selected issues
        List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        for (Issue issue : selectedIssues)
        {
            if (!permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue, remoteUser))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser, Context taskContext)
            throws BulkOperationException
    {
        List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        // Check if mail should be sent for this bulk operation
        boolean sendMail = bulkEditBean.isSendBulkNotification();

        for (Issue issue : selectedIssues)
        {
            Context.Task task = taskContext.start(issue);
            // During bulk delete an issue could have been removed as it is a sub-task of another issue
            // Hence, we need to check whether the issue is actually still in the database
            if (issueManager.getIssueObject(issue.getId()) != null)
            {
                try
                {
                    issueManager.deleteIssue(applicationUser.getDirectoryUser(), issue,
                            EventDispatchOption.ISSUE_DELETED, sendMail);
                }
                catch (RemoveException e)
                {
                    throw new BulkOperationException(e);
                }
            }
            else if (log.isDebugEnabled())
            {
                log.debug("Not deleting issue with id '" + issue.getId() + "' and key '" + issue.getKey() +
                        "' as it does not exist in the database (it could have been deleted earlier as it might be a subtask).");
            }
            task.complete();
        }
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getSelectedIssues().size();
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public boolean equals(Object o)
    {
        return this == o || o instanceof BulkDeleteOperation;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }
}
