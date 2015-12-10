package com.atlassian.jira.web.action.issue;

/**
 * Utility methods that allow you to get the limits that are imposed by JIRA configuration properties.
 *
 * @since v4.3
 */
public interface IssueSearchLimits
{
    /**
     * The default maximum for the number of issues returned by a search.
     */
    final int DEFAULT_MAX_RESULTS = 1000;

    /**
     * Returns the maximum number of search results that this JIRA instance is configured to allow, by reading it from
     * the <pre>jpm.xml</pre> file. If there is a problem reading the configured value, this method
     * returns {@value #DEFAULT_MAX_RESULTS}.
     *
     * @return an int containing the maximum number of search results to return
     */
    public int getMaxResults();
}
