package com.atlassian.jira.jql.builder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the logical operators that the builder deals with.
 *
 * @since 4.0.
 */
enum BuilderOperator
{
    //NOTE: The order of these operators is important. The BuilderOperator.compareTo method must
    //order them in precedence order.
    LPAREN
    {
        MutableClause createClauseForOperator(final MutableClause left, final MutableClause right)
        {
            throw new UnsupportedOperationException();
        }
    },
    RPAREN
    {
        MutableClause createClauseForOperator(final MutableClause left, final MutableClause right)
        {
            throw new UnsupportedOperationException();
        }
    },
    OR
    {
        MutableClause createClauseForOperator(final MutableClause left, final MutableClause right)
        {
            return new MultiMutableClause(this, notNull("left", left), notNull("right", right));
        }
    },
    AND
    {
        MutableClause createClauseForOperator(final MutableClause left, final MutableClause right)
        {
            return new MultiMutableClause(this, notNull("left", left), notNull("right", right));
        }
    },
    NOT
    {
        MutableClause createClauseForOperator(final MutableClause left, final MutableClause right)
        {
            return new NotMutableClause(notNull("left", left));
        }
    };

    /**
     * Return a new clause that combines left and right clauses with the current operator.
     *
     * @param left the left hand clause. Cannot be null.
     * @param right the right hand clause. Can be null for operators that do not take two clauses.
     *
     * @return a new clause that combines the past clauses with the current operator.
     */
    abstract MutableClause createClauseForOperator(final MutableClause left, final MutableClause right);
}
