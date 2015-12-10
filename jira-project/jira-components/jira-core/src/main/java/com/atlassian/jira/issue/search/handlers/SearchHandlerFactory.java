package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchHandler;

/**
 * Factory to create {@link SearchHandler} instances.
 *
 * @since v4.0
 */
public interface SearchHandlerFactory
{
    /**
     * Create the {@link com.atlassian.jira.issue.search.SearchHandler} using for the passed field.
     *
     * @param field the field to create the handler for.
     * @return a new SearchHandler for the passed field. Should never return null.
     */
    SearchHandler createHandler(SearchableField field);
}
