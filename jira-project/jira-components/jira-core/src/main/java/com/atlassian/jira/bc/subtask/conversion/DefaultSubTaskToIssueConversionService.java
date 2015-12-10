package com.atlassian.jira.bc.subtask.conversion;

import java.util.Collection;
import java.util.Iterator;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowManager;

/**
 * Default implementation of {@link SubTaskToIssueConversionService}.
 */
public class DefaultSubTaskToIssueConversionService extends DefaultIssueConversionService implements SubTaskToIssueConversionService
{

    private final IssueLinkManager issueLinkManager;
    private final SubTaskManager subTaskManager;

    public DefaultSubTaskToIssueConversionService(PermissionManager permissionManager,
                                                  WorkflowManager workflowManager,
                                                  FieldLayoutManager fieldLayoutManager,
                                                  IssueTypeSchemeManager issueTypeSchemeManager,
                                                  JiraAuthenticationContext jiraAuthenticationContext,
                                                  FieldManager fieldManager,
                                                  IssueLinkManager issueLinkManager,
                                                  SubTaskManager subTaskManager,
                                                  IssueEventManager issueEventManager,
                                                  IssueEventBundleFactory issueEventBundleFactory)
    {
        super(permissionManager, workflowManager, fieldLayoutManager, issueTypeSchemeManager, jiraAuthenticationContext, fieldManager, issueEventManager, issueEventBundleFactory);
        this.issueLinkManager = issueLinkManager;
        this.subTaskManager = subTaskManager;
    }

    public boolean canConvertIssue(JiraServiceContext context, Issue issue)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        if (issue == null)
        {
            throw new IllegalArgumentException("Issue must not be null");
        }

        if (!issue.isSubTask())
        {
            errorCollection.addErrorMessage(getText("convert.subtask.to.issue.errormessage.issuenotsubtask", issue.getKey())); // can only convert sub-tasks
        }

        if (!hasPermission(context, issue))
        {
            if (context.getLoggedInUser() == null)
            {
                errorCollection.addErrorMessage(getText("convert.subtask.to.issue.errormessage.nopermissionanon"));
            }
            else
            {
                errorCollection.addErrorMessage(getText("convert.subtask.to.issue.errormessage.nopermisionuser"));
            }
        }

        context.getErrorCollection().addErrorCollection(errorCollection);

        return !errorCollection.hasAnyErrors();
    }

    public void validateTargetIssueType(JiraServiceContext context, Issue issue, IssueType issueType, final String fieldNameIssueTypeId)
    {
        final ErrorCollection errorCollection = context.getErrorCollection();

        if (issueType.isSubTask())
        {
            errorCollection.addError(fieldNameIssueTypeId, getText("convert.subtask.to.issue.error.issuetypenotsubtask", issueType.getName())); // Issue Type must be a sub-task
        }

        final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(issue.getProjectObject());
        boolean found = false;

        for (Iterator<IssueType> it = issueTypes.iterator(); it.hasNext() && !found;)
        {
            IssueType type = it.next();
            if (type.getId().equals(issueType.getId()))
            {
                found = true;
            }
        }
        if (!found)
        {
            errorCollection.addError(fieldNameIssueTypeId, getText("convert.issue.to.subtask.error.issuetypenotforproject", issueType.getName())); // Issue type not applicable for project
        }
    }

    /**
     * A sub-task inherits its Security Level from its parent.  As this issue will now no longer
     * be a sub-task it must set its Security Level if required.
     */
    protected boolean canIssueSecurityFieldIgnore()
    {
        return false;
    }

    /**
     * Removes the parent link and adds change item.
     *
     * @param context jira service context
     */
    public void preStoreUpdates(JiraServiceContext context, IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue)
    {
        Collection<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(currentIssue.getId());
        for (final IssueLink issueLink : inwardLinks)
        {
            if (issueLink.getIssueLinkType().isSubTaskLinkType())
            {
                try
                {
                    issueLinkManager.removeIssueLink(issueLink, context.getLoggedInUser());
                }
                catch (RemoveException e)
                {
                    throw new DataAccessException(e);
                }
            }
        }

        // need to reorder sequence.
        subTaskManager.resetSequences(currentIssue.getParentObject());

        final Issue parentIssue = currentIssue.getParentObject();
        changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Parent", parentIssue.getId().toString(), parentIssue.getKey(), null, null));
    }

}
