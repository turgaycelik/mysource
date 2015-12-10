package com.atlassian.jira.rest.v2.issue.watcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.rest.v2.issue.WatchersBean;

/**
 * This interface specifies the methods available for watcher-specific functionality in the REST plugin.
 *
 * @since v4.2
 */
public interface WatcherOps
{
    /**
     * Retrieves a WatchersBean for the given Issue on behalf of a remote user.
     *
     * @param issue an Issue
     * @param remoteUser a User representing the remote user
     * @return a WatchersBean
     */
    WatchersBean getWatchers(Issue issue, User remoteUser);

    /**
     * Retrieves a WatchersBean with only the count, not the full list of watcher users.
     * @param issue an Issue
     * @param remoteUser the remote user
     * @return a WatchersBean with only the count
     */
    WatchersBean getWatcherCount(Issue issue, User remoteUser);
}
