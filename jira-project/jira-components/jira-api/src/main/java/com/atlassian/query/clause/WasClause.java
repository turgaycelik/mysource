package com.atlassian.query.clause;

import com.atlassian.query.history.HistoryPredicate;

/**
 * Used to represent WAS in the Query tree
 *
 * @since v4.3
 */
public interface WasClause extends TerminalClause
{
    String getField();

    HistoryPredicate getPredicate();
}
