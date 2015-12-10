package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.web.util.AttachmentException;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Well-behaved MessageHandler implementations should use as much as possible
 * this interface to create appropriate entities.
 * Depending on the mode the handler is run in the calls will be mutative for JIRA (in normal
 * production run) or will create just dummy objects when run while testing message handler
 * from administration screen.
 *
 * @since v5.0
 */
@PublicApi
public interface MessageHandlerContext
{
    /**
     * Creates user in JIRA or just dummy user if run in dry run mode
     * @param username
     * @param password
     * @param email
     * @param fullname
     * @param userEventType
     * @return
     * @throws PermissionException
     * @throws CreateException
     */
    User createUser(String username, String password, String email, String fullname, Integer userEventType)
            throws PermissionException, CreateException;

    /**
     * Creates a new issue comment in JIRA or a dummy comment in dry run mode
     * @param issue
     * @param author
     * @param body
     * @param dispatchEvent
     * @return
     */
    Comment createComment(Issue issue, User author, String body, boolean dispatchEvent);

    /**
     * Creates a new issue in JIRA or a dummy issue in dry run mode
     *
     * @param reporter
     * @param issue
     * @return
     * @throws CreateException
     */
    Issue createIssue(@Nullable User reporter, Issue issue) throws CreateException;

    /**
     * Creates attachment (when run in real mode) or does nothing if run in dry run mode.
     * @param file
     * @param filename
     * @param contentType
     * @param author
     * @param issue
     * @return a bean which should be passed later (if run in real mode) to {@link com.atlassian.jira.issue.util.IssueUpdater#doUpdate(com.atlassian.jira.issue.util.IssueUpdateBean, boolean)}
     *
     * @throws AttachmentException
     */
    ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue) throws AttachmentException;

    /**
     * @return <code>true</code> if the context works in production mode (when handlers are run from the service)
     * or <code>false</code> if runs
     */
    boolean isRealRun();

    /**
     * @return place where message handler should report its progress and problems with processing the message.
     * Depending on the run mode it will be either logged in the log file (when real run) or showed to the user
     * in UI (when run in test mode via configuring handler from UI)
     */
    MessageHandlerExecutionMonitor getMonitor();
}
