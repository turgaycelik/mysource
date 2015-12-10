package com.atlassian.jira.jql.validator;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * Validates a clause and adds human readable i18n'ed messages if there is a problem.
 *
 * @since v4.0
 */
public interface ClauseValidator
{
    /**
     * Validates a clause and adds human readable i18n'ed messages if there is a problem.
     *
     * @param searcher the user who is executing the search.
     * @param terminalClause the clause to validate.
     *
     * @return an MessageSet that will contain any messages relating to failed validation. An empty message set must
     * be returned to indicate there were no errors. null can never be returned.
     */
    @Nonnull
    MessageSet validate(User searcher, @Nonnull TerminalClause terminalClause);
}
