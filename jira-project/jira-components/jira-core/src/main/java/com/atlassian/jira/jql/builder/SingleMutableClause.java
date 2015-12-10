package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.Clause;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link MutableClause} that holds one JQL clause.
 *
 * @since v4.0
 */
class SingleMutableClause implements MutableClause
{
    private final Clause clause;

    SingleMutableClause(Clause clause)
    {
        this.clause = notNull("clause", clause);
    }

    public MutableClause combine(BuilderOperator logicalOperator, MutableClause otherClause)
    {
        notNull("logicalOperator", logicalOperator);
        return logicalOperator.createClauseForOperator(this, otherClause);
    }

    public Clause asClause()
    {
        return clause;
    }

    public MutableClause copy()
    {
        return this;
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

        final SingleMutableClause that = (SingleMutableClause) o;

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
        return clause.toString();
    }
}
