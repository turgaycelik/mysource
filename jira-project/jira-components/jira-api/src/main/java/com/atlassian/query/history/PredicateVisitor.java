package com.atlassian.query.history;

import com.atlassian.query.clause.AndClause;

/**
 * A visitor that allows you to perform operations on a {@link com.atlassian.query.history.HistoryPredicate}.
 *
 * @since v4.3
 */
public interface PredicateVisitor<R>
{

       /**
     * Visit called when accepting a {@link com.atlassian.query.history.HistoryPredicate}.
     *
     * @param  the node being visited.
     *
     * @return The return type specified by the visitor.
     */
    R visit(final HistoryPredicate predicate);
}
