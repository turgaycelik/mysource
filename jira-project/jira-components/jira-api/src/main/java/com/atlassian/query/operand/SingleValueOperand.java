package com.atlassian.query.operand;


import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to represent a single constant value as an Operand.
 *
 * @since v4.0
 */
public final class SingleValueOperand implements Operand
{
    public static final String OPERAND_NAME = "SingleValueOperand";

    private final Long longValue;
    private final String stringValue;

    public SingleValueOperand(String stringValue)
    {
        this.stringValue = notNull("stringValue", stringValue);
        this.longValue = null;
    }

    public SingleValueOperand(final Long longValue)
    {
        this.longValue = notNull("longValue", longValue);
        this.stringValue = null;
    }

    /**
     * Note: cannot accept an empty {@link com.atlassian.jira.jql.operand.QueryLiteral}.
     * Use {@link com.atlassian.query.operand.EmptyOperand} instead.
     *
     * @param literal the query literal to convert to an operand; must not be null or empty.
     */
    public SingleValueOperand(final QueryLiteral literal)
    {
        notNull("literal", literal);
        if (literal.getLongValue() != null)
        {
            this.longValue = literal.getLongValue();
            this.stringValue = null;
        }
        else if (literal.getStringValue() != null)
        {
            this.stringValue = literal.getStringValue();
            this.longValue = null;
        }
        else
        {
            throw new IllegalArgumentException("QueryLiteral '" + literal + "' must contain at least one non-null value");
        }
    }

    public String getName()
    {
        return OPERAND_NAME;
    }

    public String getDisplayString()
    {
        if (longValue == null)
        {
            return "\"" + stringValue + "\"";
        }
        else
        {
            return longValue.toString();
        }
    }

    public <R> R accept(final OperandVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    public Long getLongValue()
    {
        return longValue;
    }

    public String getStringValue()
    {
        return stringValue;
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

        final SingleValueOperand that = (SingleValueOperand) o;

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
        result = (longValue != null ? longValue.hashCode() : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Single Value Operand [" + getDisplayString() + "]";
    }
}
