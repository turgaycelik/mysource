package com.atlassian.jira.issue.search.util;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operator.Operator;

/**
 * Look through the query and try and find "text" searching conditions. We use a simple hueristic that any
 * TerminalClause with a "LIKE" and "NOT LIKE" means a text match.
 *
 * @since v4.0
 */
public class FreeTextVisitor implements ClauseVisitor<Boolean>
{
    public static boolean containsFreeTextCondition(Clause clause)
    {
        if (clause == null)
        {
            return false;
        }

        FreeTextVisitor visitor = new FreeTextVisitor();
        return clause.accept(visitor);
    }

    public Boolean visit(final AndClause andClause)
    {
        return doVisit(andClause);
    }

    public Boolean visit(final NotClause notClause)
    {
        return doVisit(notClause);
    }

    public Boolean visit(final OrClause orClause)
    {
        return doVisit(orClause);
    }

    public Boolean visit(final TerminalClause clause)
    {
        final Operator operator = clause.getOperator();
        return operator == Operator.LIKE || operator == Operator.NOT_LIKE;
    }

    @Override
    public Boolean visit(WasClause clause)
    {
        return false;
    }

    @Override
    public Boolean visit(ChangedClause clause)
    {
        // changed soes not support free text search
        return false;
    }


    private Boolean doVisit(final Clause andClause)
    {
        for (Clause clause : andClause.getClauses())
        {
            if (clause.accept(this))
            {
                return true;
            }
        }
        return false;
    }
}
