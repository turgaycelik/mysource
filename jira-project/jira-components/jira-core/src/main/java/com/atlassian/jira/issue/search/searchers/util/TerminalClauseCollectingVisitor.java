package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A clause collecting visitor that collects all the termical clauses in a clause tree.
 *
 * @since v4.0
 */
public class TerminalClauseCollectingVisitor extends RecursiveClauseVisitor implements ClauseVisitor<Void>
{
    private Collection<TerminalClause> clauses = new ArrayList<TerminalClause>();

    public Collection<TerminalClause> getClauses()
    {
        return clauses;
    }

    public Void visit(final TerminalClause clause)
    {
        clauses.add(clause);
        return null;
    }
}
