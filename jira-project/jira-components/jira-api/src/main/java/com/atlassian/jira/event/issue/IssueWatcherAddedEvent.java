package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 * Event that is triggered when user starts watching an issue.
 *
 * @since v5.2
 */
public class IssueWatcherAddedEvent extends AbstractEvent
{
    private final Issue issue;
    private final ApplicationUser user;

    public IssueWatcherAddedEvent(Issue issue, ApplicationUser user)
    {
        this.issue = issue;
        this.user = user;
    }

    public Issue getIssue()
    {
        return issue;
    }

    /**
     * @deprecated Use {@link #getApplicationUser()} instead. Since v6.0.
     */
    public User getUser()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    public ApplicationUser getApplicationUser()
    {
        return user;
    }
}
