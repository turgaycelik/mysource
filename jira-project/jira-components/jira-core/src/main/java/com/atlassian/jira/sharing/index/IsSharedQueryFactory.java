package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.sharing.SharedEntityColumnDefinition;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.util.ComponentLocator;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Creates a query to retrieve all entities that have been shared in any way.
 *
 * Does not retrieve entities that are private.
 *
 * @since v4.4.1
 */
public class IsSharedQueryFactory implements QueryFactory
{
    private Long systemDashboardId;
    private final ComponentLocator componentLocator;

    public IsSharedQueryFactory(final ComponentLocator componentLocator)
    {
        this.componentLocator = componentLocator;
    }

    @Override
    public Query create(SharedEntitySearchParameters searchParameters, User user)
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(SharedEntityColumnDefinition.IS_SHARED.getName(), "true")), BooleanClause.Occur.MUST);
        return query;
    }


    @Override
    public Query create(SharedEntitySearchParameters searchParameters)
    {
        throw new UnsupportedOperationException();
    }
}
