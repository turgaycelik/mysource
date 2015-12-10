package com.atlassian.jira.jql.clause;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.MultiClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;

import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Compares two clauses for equivalence using a simple algorithm.
 *
 * @since v4.0
 */
public class SimpleEquivalenceComparator
{
    /**
     * Compares two clauses for equivalence suitable for use in Issue Navigator; e.g. clauses that may actually be
     * logically equivalent, but are structured differently, would fail this test. Note that ordering is not important
     * within {@link MultiClause}s
     *
     * It is assumed that both clauses have been normalised before being passed in, so that their structures are as
     * similar as possible.
     *
     * @param clause a normalised Clause; never null
     * @param clause1 a normalised Clause; never null
     * @return true if the clauses are equivalent; false otherwise. 
     */
    public boolean isEquivalent(final Clause clause, final Clause clause1)
    {
        notNull("clause", clause);
        notNull("clause1", clause1);

        if (clause.equals(clause1))
        {
            return true;
        }
        else
        {
            if (clause instanceof NotClause && clause1 instanceof NotClause)
            {
                return compareNot((NotClause) clause, (NotClause) clause1);
            }
            else if (clause instanceof AndClause && clause1 instanceof AndClause)
            {
                return compareMulti((AndClause) clause, (AndClause) clause1);
            }
            else if (clause instanceof OrClause && clause1 instanceof OrClause)
            {
                return compareMulti((OrClause) clause, (OrClause) clause1);
            }
            else if (clause instanceof TerminalClause && clause1 instanceof TerminalClause)
            {
                return compareTerminal((TerminalClause) clause, (TerminalClause) clause1);
            }

            // if they're not the same clause, then they aint equal!
        }

        return false;
    }
           
    private boolean compareTerminal(final TerminalClause terminalClause, final TerminalClause terminalClause1)
    {
        if (!terminalClause.getName().equals(terminalClause1.getName()))
        {
            return false;
        }
        else if (!terminalClause.getOperator().equals(terminalClause1.getOperator()))
        {
            return false;
        }                             
        else
        {
            // operand ordering could be different but equivalent - need to check
            return new SimpleOperandComparator().isEquivalent(terminalClause.getOperand(), terminalClause1.getOperand());
        }
    }

    private boolean compareNot(final NotClause notClause, final NotClause notClause1)
    {
        return isEquivalent(notClause.getSubClause(), notClause1.getSubClause());
    }

    private boolean compareMulti(final MultiClause clause, final MultiClause clause1)
    {
        List<Clause> children = clause.getClauses();
        List<Clause> children1 = new LinkedList<Clause>(clause1.getClauses());

        if (children.size() != children1.size())
        {
            return false;
        }

        for (Clause child : children)
        {
            boolean found = false;
            for (Clause child1 : children1)
            {
                if (isEquivalent(child, child1))
                {
                    found = true;
                    children1.remove(child1);
                    break;
                }
            }

            if (!found)
            {
                return false;
            }
        }

        return true;
    }
}
