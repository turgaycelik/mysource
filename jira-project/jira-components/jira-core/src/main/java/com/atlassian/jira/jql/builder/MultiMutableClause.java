package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link MutableClause} that represents a collection of MutableClauses joined by
 * either an {@link BuilderOperator#AND} or an {@link
 * BuilderOperator#OR}.
 *
 * @since v4.0
 */
class MultiMutableClause implements MutableClause
{
    private final List<MutableClause> clauses = new LinkedList<MutableClause>();
    private final BuilderOperator logicalOperator;

    MultiMutableClause(BuilderOperator logicalOperator, MutableClause... clauses)
    {
        this(logicalOperator, Arrays.asList(clauses));
    }

    MultiMutableClause(BuilderOperator logicalOperator, Collection<? extends MutableClause> clauses)
    {
        if (logicalOperator != BuilderOperator.AND && logicalOperator != BuilderOperator.OR)
        {
            throw new IllegalArgumentException("logicalOperator must be 'AND' or 'OR'.");
        }

        notNull("clauses", clauses);
        not("clauses is empty", clauses.isEmpty());

        this.logicalOperator = logicalOperator;
        this.clauses.addAll(containsNoNulls("clauses", clauses));
    }

    public MutableClause combine(final BuilderOperator logicalOperator, final MutableClause otherClause)
    {
        notNull("logicalOperator", logicalOperator);
        if (this.logicalOperator == logicalOperator)
        {
            notNull("otherClause", otherClause);
            clauses.add(otherClause);
            return this;
        }
        else
        {
            return logicalOperator.createClauseForOperator(this, otherClause);
        }
    }

    public Clause asClause()
    {
        final List<Clause> newClauses = new ArrayList<Clause>(clauses.size());
        for (MutableClause mutableClause : clauses)
        {
            final Clause clause = mutableClause.asClause();
            if (clause != null)
            {
                newClauses.add(clause);
            }
        }

        if (newClauses.isEmpty())
        {
            return null;
        }
        else if (newClauses.size() == 1)
        {
            return newClauses.get(0);
        }
        else
        {
            if (logicalOperator == BuilderOperator.AND)
            {
                return new AndClause(newClauses);
            }
            else if (logicalOperator == BuilderOperator.OR)
            {
                return new OrClause(newClauses);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    public MutableClause copy()
    {
        List<MutableClause> copiedClauses = new ArrayList<MutableClause>(clauses.size());
        for (MutableClause mutableClause : clauses)
        {
            copiedClauses.add(mutableClause.copy());
        }

        return new MultiMutableClause(logicalOperator, copiedClauses);
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

        final MultiMutableClause that = (MultiMutableClause) o;

        if (!clauses.equals(that.clauses))
        {
            return false;
        }
        if (logicalOperator != that.logicalOperator)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = clauses.hashCode();
        result = 31 * result + logicalOperator.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("(");
        boolean first = true;
        for (MutableClause clause : clauses)
        {
            if (!first)
            {
                builder.append(' ').append(logicalOperator).append(' ');
            }
            else
            {
                first = false;
            }

            builder.append(clause);
        }
        return builder.append(")").toString();
    }
}
