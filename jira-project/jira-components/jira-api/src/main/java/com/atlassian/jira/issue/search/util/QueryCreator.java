package com.atlassian.jira.issue.search.util;

/**
 * Converts quicksearch language search queries into JIRA search URLs.
 *
 * @since v4.0
 */
public interface QueryCreator
{
    String QUERY_PREFIX = "IssueNavigator.jspa?reset=true&mode=show";
    String NULL_QUERY = "IssueNavigator.jspa?mode=show";

    /**
     * Create a URL to be redirected to, given a search query.  The URL does not include context path.
     * <p/>
     * It will do some smart searching based on whether there is a project selected.
     *
     * @param searchString the special "quicksearch language" search string
     */
    String createQuery(String searchString);
}
