package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Able to map clauses to query handlers.
 *
 * @since v4.0
 */
public final class DefaultQueryRegistry implements QueryRegistry
{
    private final SearchHandlerManager manager;

    public DefaultQueryRegistry(final SearchHandlerManager manager)
    {
        this.manager = notNull("manager", manager);
    }

    public Collection<ClauseQueryFactory> getClauseQueryFactory(final QueryCreationContext queryCreationContext, final TerminalClause clause)
    {
        notNull("clause", clause);
        final Collection<ClauseHandler> handlers;
        if (!queryCreationContext.isSecurityOverriden())
        {
            handlers = manager.getClauseHandler(queryCreationContext.getQueryUser(), clause.getName());
        }
        else
        {
            handlers = manager.getClauseHandler(clause.getName());
        }
        // Collect the factories.
        // JRA-23141 : We avoid using a lazy transformed collection here because it gets accessed multiple times
        // and size() in particular is slow.
        List<ClauseQueryFactory> clauseQueryFactories = new ArrayList<ClauseQueryFactory>(handlers.size());
        for (ClauseHandler clauseHandler : handlers)
        {
            clauseQueryFactories.add(clauseHandler.getFactory());
        }
        return clauseQueryFactories;
    }
}
