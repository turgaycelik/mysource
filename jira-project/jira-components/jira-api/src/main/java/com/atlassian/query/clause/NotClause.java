package com.atlassian.query.clause;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collections;
import java.util.List;

/**
 * Used to represent a logical NOT in the query tree.
 */
public class NotClause implements Clause
{
    public static final String NOT = "NOT";

    private final Clause subClause;

    public NotClause(Clause subClause)
    {
        this.subClause = Assertions.notNull("subClause", subClause);
    }

    public String getName()
    {
        return NOT;
    }

    public List<Clause> getClauses()
    {
        return Collections.singletonList(subClause);
    }

    public <R> R accept(final ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    public Clause getSubClause()
    {
        return subClause;
    }

    public String toString()
    {
        final ClausePrecedence currentPrecedence = ClausePrecedence.getPrecedence(this);
        final ClausePrecedence subClausePrecedence = ClausePrecedence.getPrecedence(subClause);
        StringBuilder sb = new StringBuilder(NOT).append(" ");
        if (subClausePrecedence.getValue() < currentPrecedence.getValue())
        {
            sb.append("( ");
        }

        sb.append(subClause.toString());

        if (subClausePrecedence.getValue() < currentPrecedence.getValue())
        {
            sb.append(" )");
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

        NotClause notClause = (NotClause) o;

        if (!subClause.equals(notClause.subClause))
        {
            return false;
        }

        return true;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        return subClause.hashCode();
    }
    ///CLOVER:ON
}
