package com.atlassian.jira.jql.clause;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.ChangedClauseImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.MultiClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * Expands the not clauses in a clause tree using DeMorgans law
 * and flips the operators to remove the not alltogether when possible.
 *
 * @since v4.0
 */
public class DeMorgansVisitor implements ClauseVisitor<Clause>
{
    private int notCount = 0;

    public Clause visit(final NotClause notClause)
    {
        notCount++;
        Clause logicalClause = notClause.getSubClause().accept(this);
        notCount--;
        return logicalClause;
    }

    public Clause visit(final AndClause andClause)
    {
        final List<Clause> nodes = visitChildren(andClause);

        if (isNegating())
        {
            return new OrClause(nodes);
        }
        else
        {
            return new AndClause(nodes);
        }
    }

    public Clause visit(final OrClause orClause)
    {
        final List<Clause> nodes = visitChildren(orClause);

        if (isNegating())
        {
            return new AndClause(nodes);
        }
        else
        {
            return new OrClause(nodes);
        }
    }

    public Clause visit(final TerminalClause terminalClause)
    {
        if (isNegating())
        {
            final Operator notOperator = getNotOperator(terminalClause.getOperator());
            if (notOperator != null)
            {
                return new TerminalClauseImpl(terminalClause.getName(), notOperator, terminalClause.getOperand(), terminalClause.getProperty());
            }
            else
            {
                // NOTE: this should never happen as we have a NOT version of every operator we use.
                return new NotClause(terminalClause);
            }
        }

        return terminalClause;
    }

    @Override
    public Clause visit(WasClause clause)
    {
        if (isNegating())
        {
            return new WasClauseImpl(clause.getName(), getNotOperator(clause.getOperator()), clause.getOperand(), clause.getPredicate() );
        }
        return clause;
    }

    @Override
    public Clause visit(ChangedClause clause)
    {
        if (isNegating())
        {
            return new ChangedClauseImpl(clause.getField(), getNotOperator(clause.getOperator()), clause.getPredicate() );
        }
        return clause;
    }

    private boolean isNegating()
    {
        // If the notCount is odd then this is true, otherwise it is false
        return (notCount % 2) == 1;
    }

    private List<Clause> visitChildren(final MultiClause multiClause)
    {
        final List<Clause> nodes = new ArrayList<Clause>(multiClause.getClauses().size());
        for (Clause logicalClause : multiClause.getClauses())
        {
            nodes.add(logicalClause.accept(this));
        }
        return nodes;
    }

    private static Operator getNotOperator(Operator operator)
    {
        switch(operator)
        {
            case IS:
                return Operator.IS_NOT;
            case IS_NOT:
                return Operator.IS;
            case IN:
                return Operator.NOT_IN;
            case NOT_IN:
                return Operator.IN;
            case LIKE:
                return Operator.NOT_LIKE;
            case NOT_LIKE:
                return Operator.LIKE;
            case EQUALS:
                return Operator.NOT_EQUALS;
            case NOT_EQUALS:
                return Operator.EQUALS;
            case GREATER_THAN:
                return Operator.LESS_THAN_EQUALS;
            case GREATER_THAN_EQUALS:
                return Operator.LESS_THAN;
            case LESS_THAN:
                return Operator.GREATER_THAN_EQUALS;
            case LESS_THAN_EQUALS:
                return Operator.GREATER_THAN;
            case WAS:
                return Operator.WAS_NOT;
            case WAS_NOT:
                return Operator.WAS;
            case CHANGED:
                return Operator.NOT_CHANGED;
            case NOT_CHANGED:
                return Operator.CHANGED;
            default:
                return null;
        }
    }
}