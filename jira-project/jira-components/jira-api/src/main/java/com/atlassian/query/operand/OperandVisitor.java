package com.atlassian.query.operand;

/**
 * A visitor that the caller to perform operations on Operands.
 *
 * @param <R> the return type from the visitor methods. Can be set to {@link Void} to indicate that the return value is
 * not important.
 */
public interface OperandVisitor<R>
{
    /**
     * The method called when visiting an {@link com.atlassian.query.operand.EmptyOperand}.
     *
     * @param empty the operand being visited.
     * @return the value to return from the operand visit.
     */
    R visit(EmptyOperand empty);

    /**
     * The method called when visiting a {@link FunctionOperand}.
     *
     * @param function the operand being visited.
     * @return the value to return from the operand visit.
     */
    R visit(FunctionOperand function);

    /**
     * The method called when visiting an {@link MultiValueOperand}.
     *
     * @param multiValue the operand being visited.
     * @return the value to return from the operand visit.
     */
    R visit(MultiValueOperand multiValue);


    /**
     * The method called when visiting an {@link com.atlassian.query.operand.SingleValueOperand}.
     *
     * @param singleValueOperand the operand being visited.
     * @return the value to return from the operand visit.
     */
    R visit(SingleValueOperand singleValueOperand);
}
