package com.atlassian.jira.service.services.mail;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.web.util.AttachmentExceedsLimitException;
import com.atlassian.jira.web.util.AttachmentException;

import javax.annotation.Nullable;
import java.io.File;

/**
* Internal use only
*
* @since v5.0
*/
@Internal
class DelegatingMessageHandlerContext implements MessageHandlerContext
{
    private final MessageHandlerContext context;
    private final ErrorAccumulatingMessageHandlerExecutionMonitor monitor;

    public DelegatingMessageHandlerContext(MessageHandlerContext context, ErrorAccumulatingMessageHandlerExecutionMonitor monitor)
    {
        this.context = context;
        this.monitor = monitor;
    }

    public User createUser(String username, String password, String email, String fullname, Integer userEventType)
            throws PermissionException, CreateException
    {
        return context.createUser(username, password, email, fullname, userEventType);
    }

    public Comment createComment(Issue issue, User author, String body, boolean dispatchEvent)
    {
        return context.createComment(issue, author, body, dispatchEvent);
    }

    public Issue createIssue(@Nullable User reporter, Issue issue) throws CreateException
    {
        return context.createIssue(reporter, issue);
    }

    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
            throws AttachmentException
    {
        ChangeItemBean cib = null;
        try
        {
            cib = context.createAttachment(file, filename, contentType, author, issue);
        }
        // Catch the exception and don't throw it to keep processing other attachments if any
        catch (AttachmentExceedsLimitException ex)
        {
            this.monitor.error(ex.getMessage());
            // Send email in this error case is necessary
            this.monitor.markMessageForForwarding(true);
        }
        return cib;
    }

    public boolean isRealRun()
    {
        return context.isRealRun();
    }

    public MessageHandlerExecutionMonitor getMonitor()
    {
        return monitor;
    }

}
