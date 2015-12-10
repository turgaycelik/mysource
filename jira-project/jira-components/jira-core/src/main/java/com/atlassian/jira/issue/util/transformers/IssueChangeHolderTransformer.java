package com.atlassian.jira.issue.util.transformers;

import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

/**
 * Transforms {@link IssueChangeHolder} bojects to the equivalent {@link IssueUpdateBean}
 */
public class IssueChangeHolderTransformer
{
    public static IssueUpdateBean toIssueUpdateBean(IssueChangeHolder issueChangeHolder, Long eventType, ApplicationUser user, boolean sendMail)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean((GenericValue) null, null, eventType, user, sendMail, issueChangeHolder.isSubtasksUpdated());
        issueUpdateBean.setChangeItems(issueChangeHolder.getChangeItems());
        issueUpdateBean.setComment(issueChangeHolder.getComment());
        return issueUpdateBean;
    }
}
