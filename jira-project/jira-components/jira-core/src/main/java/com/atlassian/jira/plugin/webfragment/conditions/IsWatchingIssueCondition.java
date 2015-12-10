package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Condition that determines whether the current user is watching the current issue.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.0
 */
public class IsWatchingIssueCondition extends AbstractIssueCondition
{
    private final WatcherManager watcherManager;

    public IsWatchingIssueCondition(WatcherManager watcherManager)
    {
        this.watcherManager = watcherManager;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        return user != null && watcherManager.isWatching(user, issue);
    }
}