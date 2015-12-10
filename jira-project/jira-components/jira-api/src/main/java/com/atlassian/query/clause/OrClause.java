package com.atlassian.query.clause;

import java.util.Arrays;
import java.util.Collection;

/**
 * Used to represent a logical OR in the query tree.
 */
public class OrClause extends MultiClause
{
    public static final String OR = "OR";

    public OrClause(final Clause... clauses)
    {
        this(Arrays.asList(clauses));
    }

    public OrClause(final Collection<? extends Clause> clauses)
    {
        super(clauses);
        if (clauses.isEmpty())
        {
            throw new IllegalArgumentException("You can not construct an OrClause without any child clauses.");
        }
    }

    public String getName()
    {
        return OR;
    }

    public <R> R accept(final ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }
}
