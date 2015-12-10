package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link MutableClause} that represents the negation of another MutableClause.
 *
 * @since v4.0
 */
class NotMutableClause implements MutableClause
{
    private final MutableClause clause;

    NotMutableClause(MutableClause clause)
    {
        this.clause = notNull("clause", clause);
    }

    public MutableClause combine(final BuilderOperator logicalOperator, final MutableClause otherClause)
    {
        notNull("logicalOperator", logicalOperator);
        return logicalOperator.createClauseForOperator(this, otherClause);
    }

    public Clause asClause()
    {
        final Clause subclause = clause.asClause();
        return subclause == null ? null : new NotClause(subclause);
    }

    public MutableClause copy()
    {
        final MutableClause copyClause = clause.copy();
        if (copyClause != clause)
        {
            return new NotMutableClause(copyClause);
        }
        else
        {
            return this;
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final NotMutableClause that = (NotMutableClause) o;

        if (!clause.equals(that.clause))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return clause.hashCode();
    }

    @Override
    public String toString()
    {
        return BuilderOperator.NOT.toString() + '(' + clause + ')';
    }
}
