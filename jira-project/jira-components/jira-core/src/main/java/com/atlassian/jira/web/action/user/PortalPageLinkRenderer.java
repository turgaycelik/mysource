package com.atlassian.jira.web.action.user;

/**
 * Creates a URL given a dashboard.
 *
 * @since v4.4.1
 */
public interface PortalPageLinkRenderer
{
    /**
     * Creates an &lt;a&gt; tag the name of the dashboard.
     * @param id the id of the dashboard.
     * @param name the name of the dashboard.
     * @return a String containing the entire html tag that links the name of the filter.
     */
    String render(Long id, String name);
}
