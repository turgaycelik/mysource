package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.TerminalClause;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An intersecting {@link com.atlassian.jira.jql.context.ClauseContextFactory} that intersectects
 * the generated query contexts of its sub {@link com.atlassian.jira.jql.context.ClauseContextFactory}'s
 *
 * @since v4.0
 */
public class IntersectingClauseContextFactory implements ClauseContextFactory
{
    private final ContextSetUtil contextSetUtil;
    private final Collection<ClauseContextFactory> subClauseContextFactories;

    public IntersectingClauseContextFactory(ContextSetUtil contextSetUtil, Collection<ClauseContextFactory> subClauseContextFactories)
    {
        this.subClauseContextFactories = containsNoNulls("subClauseContextFactories", subClauseContextFactories);
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        Set<ClauseContext> clauseContexts = new HashSet<ClauseContext>();
        for (ClauseContextFactory subClauseContextFactory : subClauseContextFactories)
        {
            clauseContexts.add(subClauseContextFactory.getClauseContext(searcher, terminalClause));
        }
        return contextSetUtil.intersect(clauseContexts);
    }
}
