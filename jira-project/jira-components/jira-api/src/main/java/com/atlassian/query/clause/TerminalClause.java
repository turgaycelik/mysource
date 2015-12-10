package com.atlassian.query.clause;

import com.atlassian.fugue.Option;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

/**
 * Denotes a terminal nodes that contain an Operator and an Operand.
 *
 * @since v4.0
 */
public interface TerminalClause extends Clause
{

    /**
     * @return the right hand side value of the expression. This can be a composite of more Operands or it can be
     *         SingleValueOperands that resolve to constant values.
     */
    Operand getOperand();

    /**
     * @return the operator used by the clause {@link com.atlassian.query.operator.Operator}.
     */
    Operator getOperator();

    /**
     * @return the name of the property or absent.
     */
    Option<Property> getProperty();
}
