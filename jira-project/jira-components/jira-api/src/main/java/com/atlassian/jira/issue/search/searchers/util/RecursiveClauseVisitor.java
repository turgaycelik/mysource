package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;

/**
 * A base clause visitor that recursively visits each clause in a clause tree.
 *
 * @since v4.0
 */
public class RecursiveClauseVisitor implements ClauseVisitor<Void>
{
   public Void visit(final AndClause andClause)
    {
        for (Clause clause : andClause.getClauses())
        {
            clause.accept(this);
        }
        return null;
    }

    public Void visit(final NotClause notClause)
    {
        return notClause.getSubClause().accept(this);
    }

    public Void visit(final OrClause orClause)
    {
        for (Clause clause : orClause.getClauses())
        {
            clause.accept(this);
        }
        return null;
    }

    public Void visit(final TerminalClause clause)
    {
        return null;
    }

    @Override
    public Void visit(WasClause clause)
    {
        return null;
    }

    //ChangedClause is a terminal clause it has no subclauses
    @Override
    public Void visit(ChangedClause clause)
    {
        return null;
    }
}
