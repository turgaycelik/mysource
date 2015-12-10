package com.atlassian.query.operand;

/**
 * Used to represent a value that has not been set for a field.
 *
 * @since v4.0
 */
public class EmptyOperand implements Operand
{
    public static final String OPERAND_NAME = "EMPTY";
    public static final EmptyOperand EMPTY = new EmptyOperand();

    public String getName()
    {
        return OPERAND_NAME;
    }

    public String getDisplayString()
    {
        return OPERAND_NAME;
    }

    public <R> R accept(final OperandVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

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

        return true;
    }

    public int hashCode()
    {
        return OPERAND_NAME.hashCode();
    }

}
