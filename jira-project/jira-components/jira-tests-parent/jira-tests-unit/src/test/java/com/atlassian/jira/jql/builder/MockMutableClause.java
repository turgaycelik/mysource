package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.Clause;

/**
 * A test that can be told to return a specific {@link com.atlassian.query.clause.Clause}.
 *
 * @since v4.0
 */
class MockMutableClause implements MutableClause
{
    private Clause clause;
    private String message;

    MockMutableClause(final Clause clause, final String message)
    {
        this.clause = clause;
        this.message = message;
    }

    MockMutableClause(final Clause clause)
    {
        this(clause, null);
    }

    void setClause(final Clause clause)
    {
        this.clause = clause;
    }

    Clause getClause()
    {
        return clause;
    }

    String getMessage()
    {
        return message;
    }

    void setMessage(final String message)
    {
        this.message = message;
    }

    public MutableClause combine(final BuilderOperator logicalOperator, final MutableClause otherClause)
    {
        return this;
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

        final MockMutableClause that = (MockMutableClause) o;

        if (clause != null ? !clause.equals(that.clause) : that.clause != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return clause != null ? clause.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return message;
    }
}
