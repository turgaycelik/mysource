package com.atlassian.jira.jql;

import com.atlassian.jira.jql.values.ClauseValuesGenerator;

/**
 * Implement this if you want to participate in the JQL autocomplete functionality.
 *
 * @since v4.0
 */
public interface ValueGeneratingClauseHandler
{

    ClauseValuesGenerator getClauseValuesGenerator();
}
