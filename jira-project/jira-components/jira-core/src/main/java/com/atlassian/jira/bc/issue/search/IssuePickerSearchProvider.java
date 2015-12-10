package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;

/**
 * Interface used by {@link com.atlassian.jira.bc.issue.search.DefaultIssuePickerSearchService} to retrieve issue matches.
 */
public interface IssuePickerSearchProvider
{
    /**
     * Returns a list of issues matching the query string
     *
     * @param context           service context
     * @param issuePickerParams params for issue picker
     * @param issueRemaining    Number of issues still needed.
     * @return list of issues that match query (implementation specific)   which is never null.
     */
    public IssuePickerResults getResults(JiraServiceContext context, IssuePickerSearchService.IssuePickerParameters issuePickerParams, int issueRemaining);

    /**
     * Indicates to the caller whether or not this provider can handle the passed parameters.
     *
     * @param searcher the user performing the search.
     * @param issuePickerParams the parameters to check.
     *
     * @return true if the provider can handle the passed parameters false otherwise.
     */
    public boolean handlesParameters(User searcher, IssuePickerSearchService.IssuePickerParameters issuePickerParams);
}
