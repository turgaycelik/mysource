package com.atlassian.query.clause;

import java.util.Arrays;
import java.util.Collection;

/**
 * Used to represent a logical AND in the query tree.
 */
public class AndClause extends MultiClause
{
    public static final String AND = "AND";

    public AndClause(final Clause... clauses)
    {
        this(Arrays.asList(clauses));
    }

    public AndClause(final Collection<? extends Clause> clauses)
    {
        super(clauses);
        if (clauses.isEmpty())
        {
            throw new IllegalArgumentException("You can not construct an AndClause without any child clauses.");
        }
    }

    public String getName()
    {
        return AND;
    }

    public <R> R accept(final ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }
}
