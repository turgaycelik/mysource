package com.atlassian.query.clause;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class that will contain multiple clauses.
 */
public abstract class MultiClause implements Clause
{
    private final List<Clause> clauses;

    protected MultiClause(final Collection<? extends Clause> clauses)
    {
        Assertions.containsNoNulls("clauses",clauses);
        this.clauses = Collections.unmodifiableList(new ArrayList<Clause>(clauses));
    }

    public List<Clause> getClauses()
    {
        return clauses;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        final ClausePrecedence currentPrecedence = ClausePrecedence.getPrecedence(this);
        for (Iterator<Clause> clauseIterator = getClauses().iterator(); clauseIterator.hasNext();)
        {
            Clause clause = clauseIterator.next();
            final ClausePrecedence childPrecedence = ClausePrecedence.getPrecedence(clause);
            if (childPrecedence.getValue() < currentPrecedence.getValue())
            {
                sb.append("( ");
            }
            sb.append(clause.toString());
            if (childPrecedence.getValue() < currentPrecedence.getValue())
            {
                sb.append(" )");
            }

            if (clauseIterator.hasNext())
            {
                sb.append(" ").append(getName()).append(" ");
            }
        }
        return sb.toString();
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MultiClause that = (MultiClause) o;

        if (clauses != null ? !clauses.equals(that.clauses) : that.clauses != null)
        {
            return false;
        }

        return true;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        return (clauses != null ? clauses.hashCode() : 0);
    }
    ///CLOVER:ON
}
