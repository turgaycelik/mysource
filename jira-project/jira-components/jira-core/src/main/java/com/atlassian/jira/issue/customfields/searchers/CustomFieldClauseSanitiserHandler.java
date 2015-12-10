package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.jql.permission.ClauseSanitiser;

/**
 * <p>Used to indicate that a particular custom field requires its clauses to be sanitised by a
 * {@link com.atlassian.jira.jql.permission.ClauseSanitiser}. The mechanism for registering the sanitiser is through the
 * {@link com.atlassian.jira.jql.permission.ClausePermissionHandler}, which is constructed by the
 * {@link com.atlassian.jira.issue.fields.SearchableField#createAssociatedSearchHandler()} method.
 *
 * <p>Thus, when implementing a {@link com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler},
 * ensure that it also implements this interface if it has special sanitisation requirements. Otherwise, the default
 * {@link com.atlassian.jira.jql.permission.NoOpClauseSanitiser} will be used. This searcher clause handler should then
 * be constructed inside the {@link com.atlassian.jira.issue.customfields.CustomFieldSearcher}.
 *
 * @see com.atlassian.jira.jql.permission.ClauseSanitiser
 * @since v4.0
 */
public interface CustomFieldClauseSanitiserHandler
{
    /**
     * @return the clause sanitiser to use with this custom field searcher clause handler
     */
    ClauseSanitiser getClauseSanitiser();
}
