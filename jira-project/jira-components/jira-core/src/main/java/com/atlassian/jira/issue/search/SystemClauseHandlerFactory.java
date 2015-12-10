package com.atlassian.jira.issue.search;

import java.util.Collection;

/**
 * Provides access to System clause handlers, through {@link com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration}'s
 * for clauses that do not have associated system fields and searchers.
 *
 * @since v4.0
 */
public interface SystemClauseHandlerFactory
{

    /**
     * Will return a collection of SearchHandlers that represent the system clause search handlers that are not
     * associated with a field or a searcher.
     *
     * @return SearchHandlers that represent the system clause search handlers that are not
     * associated with a field or a searcher.
     */
    Collection<SearchHandler> getSystemClauseSearchHandlers();
}
