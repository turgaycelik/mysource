package com.atlassian.jira.web.action.filter;

/**
 * Creates a URL given a filter.
 *
 * @since v3.13
 */
public interface FilterLinkRenderer
{
    /**
     * Creates an &lt;a&gt; tag the name of the filter.
     * @param id the id of the filter.
     * @param name the name of the filter.
     * @return a String containing the entire html tag that links the name of the filter.
     */
    String render(Long id, String name);
}
