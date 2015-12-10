package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Locale;

/**
 * Get all watchers for an issue.
 *
 * @since v4.1
 */
public interface IssueWatcherAccessor
{
    boolean isWatchingEnabled();

    /**
     * Convenience function that simply returns the User names.
     *
     * @param issue the issue to get the watchers for
     * @return an Iterable of the user names, empty if no watchers
     *
     * @deprecated Use {@link #getWatcherKeys(com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    @Nonnull
    Iterable<String> getWatcherNames(final @Nonnull Issue issue);

    Collection<String> getWatcherKeys(Issue issue);

    /**
     * Convenience function that simply returns the User objects.
     *
     * @param displayLocale for sorting.
     * @param issue the issue to get the watchers for
     * @return an Iterable of the users, empty if no watchers
     * @since v4.3
     */
    @Nonnull
    Iterable<User> getWatchers(final @Nonnull Issue issue, final @Nonnull Locale displayLocale);
}
