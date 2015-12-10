package com.atlassian.query.clause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

/**
 * Represents the clause for the "WAS mode" of change history querying. This clause selects issues on the basis of a
 * previous value of a field being equal to a given value (the 'operand').
 *
 * @since v4.3
 */
public final class WasClauseImpl implements WasClause
{

    String field;
    Operand operand;
    Operator operator;
    HistoryPredicate predicate;
    List<Clause> subClauses = new ArrayList<Clause>();
    private String displayString="was";

    public WasClauseImpl(String field, Operator operator, Operand operand, HistoryPredicate predicate)
    {
        this.field = Assertions.notNull("field", field);
        this.operand = Assertions.notNull("operand",operand);
        this.operator=operator;
        this.predicate = predicate;
    }

    public WasClauseImpl(WasClause clause)
    {
        Assertions.notNull("Was Clause", clause);
        this.field=clause.getField();
        this.operand=clause.getOperand();
        this.predicate=clause.getPredicate();
        this.operator=clause.getOperator();
    }

    @Override
    public <R> R accept(ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public String getName()
    {
        return getField();
    }

    @Override
    public List<Clause> getClauses()
    {
        return Collections.unmodifiableList(subClauses);
    }

    public Operand getOperand()
    {
        return operand;
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public Option<Property> getProperty()
    {
        return Option.none();
    }

    @Override
    public String getField()
    {
        return field;
    }

    @Override
    public HistoryPredicate getPredicate()
    {
        return predicate;
    }

    public String toString()
    {
        //The '{' brackets in this method are designed to make this method return invalid JQL so that we know when
        //we call this method. This method is only here for debugging and should not be used in production.
        StringBuilder sb = new StringBuilder("{").append(getName());

        sb.append(" ")
                .append("was")
                .append(" ")
                .append(operand.getDisplayString());
        if (predicate != null)
        {
           sb.append(" ").append(predicate.getDisplayString());
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

        final WasClauseImpl that = (WasClauseImpl) o;

        if (!field.equals(that.field))
        {
            return false;
        }
        if (!operand.equals(that.operand))
        {
            return false;
        }
        if ((predicate != null) && !predicate.equals(that.predicate))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = operand.hashCode();
        if (predicate != null)
        result = 31 * result + (predicate == null? 0: predicate.hashCode());
        result = 31 * result + field.hashCode();
        return result;
    }


    public String getDisplayString()
    {
        return displayString;
    }

}
