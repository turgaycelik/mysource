package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;

/**
 * A {@link com.atlassian.jira.jql.validator.OperatorUsageValidator} that assumes the
 * passed argument is always valid.
 *
 * @since v4.0
 */
public class AlwaysValidOperatorUsageValidator implements OperatorUsageValidator
{
    public MessageSet validate(final User searcher, final TerminalClause clause)
    {
        return new MessageSetImpl();
    }

    public boolean check(final User searcher, final TerminalClause clause)
    {
        return true;
    }
}
