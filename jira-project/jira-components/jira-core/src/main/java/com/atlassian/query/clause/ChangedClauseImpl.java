package com.atlassian.query.clause;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Implementation of ChangedClasue
 *
 * @since v5.0
 */
public class ChangedClauseImpl implements ChangedClause
{
    private final String field;
    private final HistoryPredicate historyPredicate;
    private final Operator operator;
    private final static String CHANGED="CHANGED";
    private final List<Clause> subClauses = Lists.newArrayList();



    public ChangedClauseImpl(final String field, Operator operator, final HistoryPredicate historyPredicate) {
        this.field = field;
        this.operator = operator;
        this.historyPredicate = historyPredicate;
    }

    public ChangedClauseImpl(ChangedClause clause)
    {
        Assertions.notNull(clause);
        this.field = clause.getField();
        this.historyPredicate=clause.getPredicate();
        this.operator = clause.getOperator();
    }


    @Override
    public String getField()
    {
        return field;
    }

    @Override
    public HistoryPredicate getPredicate()
    {
        return historyPredicate;
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public String getName()
    {
        return CHANGED;
    }

    @Override
    public List<Clause> getClauses()
    {
        return subClauses;
    }

    @Override
    public <R> R accept(ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    public String toString()
    {
        //The '{' brackets in this method are designed to make this method return invalid JQL so that we know when
        //we call this method. This method is only here for debugging and should not be used in production.
        StringBuilder sb = new StringBuilder("{").append(getField());

        sb.append(" ").append("changed");
        if (historyPredicate != null)
        {
           sb.append(" ").append(historyPredicate.getDisplayString());
        }
        sb.append("}");
        return sb.toString();
    }

    ///CLOVER:OFF

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

        final ChangedClauseImpl that = (ChangedClauseImpl) o;

        if (!field.equals(that.field))
        {
            return false;
        }
        if ((historyPredicate != null) && !historyPredicate.equals(that.historyPredicate))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = field.hashCode();
        result = 31 * result + (historyPredicate == null? 0: historyPredicate.hashCode());
        return result;
    }
}
