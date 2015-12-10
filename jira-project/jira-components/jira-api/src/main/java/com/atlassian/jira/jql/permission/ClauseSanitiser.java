package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;

/**
 * Deals with the sanitisation of clauses based on the given user.
 *
 * @since v4.0
 */
public interface ClauseSanitiser
{
    /**
     * <p>Given a user and a clause, will return a sanitised clause that when possible will not contain any information
     * that the specified user does not have permission to see. For example, if the given clause names a project that
     * the user cannot browse, a sanitiser might return a new clause with the name of the project replaced with the id.
     *
     * <p>It is important that the returned clause is equivalent to the input clause, within the constraints of the
     * permissible clauses for the specified user.
     *
     * @param user the user performing the search
     * @param clause the clause to be sanitised
     * @return the sanitised clause; never null.
     */
    Clause sanitise(User user, TerminalClause clause);
}
