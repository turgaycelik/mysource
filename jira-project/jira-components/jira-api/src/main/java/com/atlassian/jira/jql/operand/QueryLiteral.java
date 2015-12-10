package com.atlassian.jira.jql.operand;

import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>Used to communicate literal values, Strings or Longs, as input into the Operands.
 *
 * <ul>
 * <li>Long values are typically used to represent ids or raw numerical values. For example, issue ids are represented
 * using a Long.
 * <li>String values are typically used to represent raw string values or named values that need to be resolved into
 * ids. For example, issue keys or project names are represented using a String.
 * </ul>
 *
 * <p>When writing {@link com.atlassian.jira.plugin.jql.function.JqlFunction}s that must return QueryLiterals, try to
 * return the more specific QueryLiteral where possible, to avoid unnecessary resolving. "More specific" here means the
 * form that is used by the index (if applicable), as this value can then be used directly when constructing index
 * queries.
 *
 * <p> QueryLiterals contain an operand source, this is the {@link com.atlassian.query.operand.Operand} of the JQL that
 * produced the QueryLiteral. For instance in the JQL query {@code project = HSP} the "HSP" QueryLiteral will have
 * the operand source of a {@link com.atlassian.query.operand.SingleValueOperand} with value "HSP". Notably
 * QueryLiterals produced by {@link com.atlassian.jira.plugin.jql.function.JqlFunction}s must set the
 * {@link com.atlassian.query.operand.FunctionOperand} as the operand source.
 *
 * @since v4.0
 */
public class QueryLiteral
{
    private final String stringValue;
    private final Long longValue;
    private final Operand sourceOperand;

    public QueryLiteral()
    {
        this.sourceOperand = EmptyOperand.EMPTY;
        this.stringValue = null;
        this.longValue = null;
    }

    public QueryLiteral(final Operand sourceOperand)
    {
        this.sourceOperand = notNull("sourceOperand", sourceOperand);
        this.stringValue = null;
        this.longValue = null;
    }

    public QueryLiteral(final Operand sourceOperand, final Long longValue)
    {
        this.sourceOperand = notNull("sourceOperand", sourceOperand);
        this.stringValue = null;
        this.longValue = longValue;
    }

    public QueryLiteral(final Operand sourceOperand, final String stringValue)
    {
        this.sourceOperand = notNull("sourceOperand", sourceOperand);
        this.stringValue = stringValue;
        this.longValue = null;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public Long getLongValue()
    {
        return longValue;
    }

    public Operand getSourceOperand()
    {
        return sourceOperand;
    }


    public boolean isEmpty()
    {
        return stringValue == null && longValue == null;
    }

    public String asString()
    {
        if (longValue != null)
        {
            return longValue.toString();
        }
        else
        {
            return stringValue;
        }
    }

    ///CLOVER:OFF

    public String toString()
    {
        return (longValue != null) ? longValue.toString() : String.valueOf(stringValue);
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

        final QueryLiteral that = (QueryLiteral) o;

        if (longValue != null ? !longValue.equals(that.longValue) : that.longValue != null)
        {
            return false;
        }
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (longValue != null ? longValue.hashCode() : 0);
        return result;
    }

    ///CLOVER:ON
}
