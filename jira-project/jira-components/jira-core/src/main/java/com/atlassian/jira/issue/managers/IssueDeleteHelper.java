package com.atlassian.jira.issue.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;

/**
 * Performs issue deletion.
 *
 * @since v4.1
 */
public interface IssueDeleteHelper
{
    /**
     * Delete <tt>issue</tt> in context of given <tt>user</tt>.
     *
     * @param user user performing the operation
     * @param issue issue to delete
     * @param eventDispatchOption event dispatching control
     * @param sendMail whether or not to send the email
     * @throws RemoveException if the removal fails
     */
    void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail) throws RemoveException;

    /**
     * Delete <tt>issue</tt> without firing any events, or sending notifications.
     * <p>
     * This is preferred in some bulk operations, but normally you would call {@link #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, com.atlassian.jira.event.type.EventDispatchOption, boolean)}
     *
     * @param issue issue to delete
     * @throws RemoveException if the removal fails
     *
     * @see #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, com.atlassian.jira.event.type.EventDispatchOption, boolean)
     */
    void deleteIssueNoEvent(Issue issue) throws RemoveException;
}
