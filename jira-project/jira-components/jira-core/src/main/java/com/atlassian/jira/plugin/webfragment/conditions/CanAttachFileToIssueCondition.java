package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

/**
 * Condition that determines whether the current user can attach a file to the current issue.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class CanAttachFileToIssueCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(CanAttachFileToIssueCondition.class);
    private final AttachmentService attachmentService;


    public CanAttachFileToIssueCondition(AttachmentService attachmentService)
    {
        this.attachmentService = attachmentService;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        JiraServiceContext context = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        return attachmentService.canCreateAttachments(context, issue);
    }

}