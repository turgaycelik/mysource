package com.atlassian.query.history;

import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

/**
 * Represents an assertion about a change history, namely that the expression of combining the prefix operator with
 * the operand. The result of evaluating the expression must be true or false. For example the operator may be
 * {@link com.atlassian.query.operator.Operator.BEFORE} and the operator (which should be a datetime).
 *
 * @since v4.3
 */
public class TerminalHistoryPredicate implements HistoryPredicate
{
    private final Operand operand;
    private final Operator operator;

    public TerminalHistoryPredicate(Operator operator, Operand operand)
    {
        this.operator = operator;
        this.operand = operand;
    }

    public Operand getOperand()
    {
        return operand;
    }

    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public String getDisplayString()
    {
        return  operator.getDisplayString() + " " + operand.getDisplayString();
    }

    @Override
    public <R> R accept(PredicateVisitor<R> visitor)
    {
           return visitor.visit(this);
    }
}
