package com.atlassian.jira.issue.watchers;

import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueWatcherAccessor implements IssueWatcherAccessor
{
    private final WatcherManager watcherManager;

    public DefaultIssueWatcherAccessor(final WatcherManager watcherManager)
    {
        this.watcherManager = notNull("voteManager", watcherManager);
    }

    @Override
    public Iterable<User> getWatchers(@Nonnull Issue issue, @Nonnull Locale displayLocale)
    {
        return watcherManager.getCurrentWatchList(issue, displayLocale);
    }

    @Override
    public boolean isWatchingEnabled()
    {
        return watcherManager.isWatchingEnabled();
    }

    @Override
    public Iterable<String> getWatcherNames(final @Nonnull Issue issue)
    {
        return watcherManager.getCurrentWatcherUsernames(issue);
    }

    @Override
    public Collection<String> getWatcherKeys(Issue issue)
    {
        return watcherManager.getWatcherUserKeys(issue);
    }
}
