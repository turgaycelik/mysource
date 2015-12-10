package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.parameters.lucene.PermissionQueryFactory;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;

import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forWatchers;
import static com.atlassian.jira.jql.query.PermissionClauseQueryFactory.create;
import static com.atlassian.jira.jql.query.QueryFactoryResult.createFalseResult;

/**
 * Factory for producing clauses for the watchers.
 *
 * @since v4.1
 */
@InjectableComponent
public class WatcherClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(WatcherClauseQueryFactory.class);

    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final WatcherManager watcherManager;

    public WatcherClauseQueryFactory(final JqlOperandResolver operandResolver, final UserResolver userResolver, final WatcherManager watcherManager, final PermissionQueryFactory permissionQueryFactory)
    {
        this.watcherManager = watcherManager;
        delegateClauseQueryFactory = create(operandResolver, userResolver, permissionQueryFactory, forWatchers().getIndexField());
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        if (watcherManager.isWatchingEnabled())
        {
            return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        }
        else
        {
            log.debug("Attempt to search watches field when watching is disabled.");
            return createFalseResult();
        }
    }
}
