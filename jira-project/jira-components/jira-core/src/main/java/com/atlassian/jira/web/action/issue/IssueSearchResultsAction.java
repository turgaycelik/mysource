package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.component.TableLayoutFactory;

/**
 * Interface that should be implemented by actions that wish to have search results displayed in the navigator
 * table.
 *
 * @since v4.0
 */
public interface IssueSearchResultsAction
{
    /**
     * Return the current search results to the caller.
     *
     * @return the current search results.
     * @throws SearchException
     */
    SearchResults getSearchResults() throws SearchException;

    /**
     * Return the object used to render the table layout.
     *
     * @return the object used to render the table layout.
     */
    TableLayoutFactory getTableLayoutFactory();

    /**
     * Get the current search request being executed.
     *
     * @return the the current search request being executed.
     */
    SearchRequest getSearchRequest();

    /**
     * Get the user executing the search.
     *
     * @return the user executing the search.
     */
    User getLoggedInUser();

    /**
     * Return the issue id that should be shown as selected.
     *
     * @return the issue id that should be shown as selected.
     */
    Long getSelectedIssueId();
}
