package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.web.bean.PagerFilter;

import java.util.Map;

/**
 * This is used by {@link com.atlassian.jira.plugin.searchrequestview.SearchRequestView} plugins to provide information about their context.
 */
public interface SearchRequestParams extends IssueViewRequestParams
{
    /**
     * Used to access information that may have been stored in the HttpSession. Values stored in the session cannot
     * be modified in any way.
     * @return map containing a shallow copy of the HttpSession
     */
    public Map getSession();

    /**
     * Used to get a handle on a pager filter that will have its start and max values setup based on either system
     * defaults or possibly overridden by request parameters ('tempMax' and 'pager/start').
     * @return PagerFilter with appropriate max and start values set.
     */
    public PagerFilter getPagerFilter();

    /**
     * @return the user agent string from the request
     */
    public String getUserAgent();

    /**
     * @param returnMax true if the search should return up to the maximum number of issues configured for the
     *                  'jira.search.views.max.limit' application property, even if a greater value is set as
     *                  the max on the pager filter.
     */
    public void setReturnMax(boolean returnMax);

    /**
     * @return true if the search should return up to the maximum number of issues configured for the
     *         'jira.search.views.max.limit' application property, even if a greater value is set as the max
     *         on the pager filter.
     */
    public boolean isReturnMax();
}
