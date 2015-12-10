package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.query.Query;
import com.google.common.collect.Collections2;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.jql.builder.JqlQueryBuilder.newBuilder;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultWatchedIssuesAccessor implements WatchedIssuesAccessor
{
    private final WatcherManager watcherManager;
    private final SearchProvider searchProvider;
    private final SearchProviderFactory factory;

    public DefaultWatchedIssuesAccessor(final @Nonnull WatcherManager watcherManager, final @Nonnull SearchProvider searchProvider, final @Nonnull SearchProviderFactory factory)
    {
        this.watcherManager = notNull("watcherManager", watcherManager);
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.factory = notNull("factory", factory);
    }

    @Override
    public Iterable<Long> getWatchedIssueIds(final User watcher, final User searcher, final Security security)
    {
        final FieldHitCollector collector = new FieldHitCollector(DocumentConstants.ISSUE_ID);
        final Query query = getQuery(watcher);
        try
        {
            switch (security)
            {
                case OVERRIDE:
                    searchProvider.searchOverrideSecurity(query, searcher, collector);
                    break;

                case RESPECT:
                    searchProvider.search(query, searcher, collector);
                    break;
            }
        }
        catch (final SearchException e)
        {
            throw new RuntimeException(e);
        }
        List<String> issueIds = collector.getValues();
        return Collections2.transform(issueIds, new com.google.common.base.Function<String, Long>()
        {
            @Override
            public Long apply(@Nullable final String input)
            {
                return Long.valueOf(input);
            }
        });
    }

    @Override
    public boolean isWatchingEnabled()
    {
        return watcherManager.isWatchingEnabled();
    }

    static Query getQuery(final User user)
    {
        return newBuilder().where().watcherUser(user.getName()).endWhere().buildQuery();
    }

}
