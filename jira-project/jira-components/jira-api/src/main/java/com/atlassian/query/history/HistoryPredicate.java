package com.atlassian.query.history;

import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

/**
 * Represents a possibly composite expression that may evaluate to true or false for a given change history item.
 * The intended use is querying the change groups of an issue to find those that contain a change item that matches
 * the predicate.
 *
 * @since v4.3
 */
public interface HistoryPredicate
{
    String getDisplayString();

    /**
     * Allows us to perform operations over the clauses based on the passed in visitor. This method calls the
     * visit method on the visitor with this reference.
     *
     * @param visitor the visitor to accept.
     *
     * @return the result of the visit operation who's type is specified by the incoming visitor.
     */
    <R> R accept (PredicateVisitor<R> visitor);

}
