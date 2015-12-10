package com.atlassian.jira.jql.operand;

import com.atlassian.query.operand.SingleValueOperand;

/**
 * Provides static methods that create QueryLiterals with {@link com.atlassian.query.operand.SingleValueOperand}
 * as the source operand with the literals value.
 *
 * @since v4.0
 */
public class SimpleLiteralFactory
{
    public static QueryLiteral createLiteral(String value)
    {
        return new QueryLiteral(new SingleValueOperand(value), value);
    }

    public static QueryLiteral createLiteral(Long value)
    {
        return new QueryLiteral(new SingleValueOperand(value), value);
    }

    public static QueryLiteral createLiteral()
    {
        return new QueryLiteral();
    }
}
