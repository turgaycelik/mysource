package com.atlassian.jira.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.query.Query;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.jql.builder.JqlQueryBuilder.newBuilder;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultVotedIssuesAccessor implements VotedIssuesAccessor
{
    private final VoteManager voteManager;
    private final SearchProvider searchProvider;
    private final SearchProviderFactory factory;

    public DefaultVotedIssuesAccessor(final @Nonnull VoteManager voteManager, final @Nonnull SearchProvider searchProvider, final @Nonnull SearchProviderFactory factory)
    {
        this.voteManager = notNull("voteManager", voteManager);
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.factory = notNull("factory", factory);
    }

    @Override
    public Iterable<Long> getVotedIssueIds(final User voter, final User searcher, final Security security)
    {
        final FieldHitCollector collector = new FieldHitCollector(DocumentConstants.ISSUE_ID);
        final Query query = getVoterQuery(voter);
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
        return Collections2.transform(issueIds, new Function<String, Long>()
        {
            @Override
            public Long apply(@Nullable final String input)
            {
                return Long.valueOf(input);
            }
        });
    }

    @Override
    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    static Query getVoterQuery(final User voter)
    {
        return newBuilder().where().voterUser(voter.getName()).endWhere().buildQuery();
    }

}
