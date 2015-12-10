package com.atlassian.query.clause;

/**
 * A visitor that allows you to perform operations on a {@link com.atlassian.query.Query}.
 *
 * @since v4.0
 */
public interface ClauseVisitor<R>
{
    /**
     * Visit called when accepting a {@link AndClause}.
     *
     * @param andClause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final AndClause andClause);

    /**
     * Visit called when accepting a {@link NotClause}.
     *
     * @param notClause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final NotClause notClause);

    /**
     * Visit called when accepting a {@link OrClause}.
     *
     * @param orClause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final OrClause orClause);

    /**
     * Visit called when accepting a {@link TerminalClause}.
     *
     * @param clause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final TerminalClause clause);


    /**
     * Visit called when accepting a {@link WasClause}.
     *
     * @param clause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final WasClause clause);  


        /**
     * Visit called when accepting a {@link ChangedClause}.
     *
     * @param clause the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final ChangedClause clause);
}
