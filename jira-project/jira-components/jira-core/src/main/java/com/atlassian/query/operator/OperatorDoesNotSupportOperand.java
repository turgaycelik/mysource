package com.atlassian.query.operator;

/**
 * Indicates a mismatch between operator and operand.
 *
 * @since v4.0
 */
public class OperatorDoesNotSupportOperand extends RuntimeException
{
    public OperatorDoesNotSupportOperand() { }

    public OperatorDoesNotSupportOperand(final String s)
    {
        super(s);
    }
}
