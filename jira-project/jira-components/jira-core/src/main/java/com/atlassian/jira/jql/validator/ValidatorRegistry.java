package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;

import java.util.Collection;

/**
 * Resolves the validators for a provided {@link com.atlassian.query.clause.TerminalClause}.
 *
 * @since v4.0
 */
public interface ValidatorRegistry
{
    /**
     * Fetches the associated ClauseValidators for the provided TerminalClause. The returned value is based on
     * the clause's name and the {@link com.atlassian.query.operator.Operator} that is associated with the
     * provided clause. Multiple values may be returned for custom fields.
     *
     * @param searcher the user who is performing the search.
     * @param clause that defines the name and operator for which we want to find a clause validator, must not be null.
     *
     * @return the validators associated with this clause, or empty list if the lookup failed.
     */
    Collection<ClauseValidator> getClauseValidator(User searcher, TerminalClause clause);

     /**
     * Fetches the associated ClauseValidators for the provided WasClause. The returned value is based on
     * the clause's name and the {@link com.atlassian.query.operator.Operator} that is associated with the
     * provided clause. Multiple values may be returned for custom fields.
     *
     * @param searcher the user who is performing the search.
     * @param clause that defines the name and operator for which we want to find a clause validator, must not be null.
     *
     * @return the validators associated with this clause, or empty list if the lookup failed.
     */
    Collection<ClauseValidator> getClauseValidator(User searcher, WasClause clause);

         /**
     * Fetches the associated ClauseValidators for the provided ChangedClause.
     *
     * @param searcher the user who is performing the search.
     * @param clause that defines the field
     *
     * @return the validators associated with this clause, or empty list if the lookup failed.
     */
    ChangedClauseValidator getClauseValidator(User searcher, ChangedClause clause);
}
