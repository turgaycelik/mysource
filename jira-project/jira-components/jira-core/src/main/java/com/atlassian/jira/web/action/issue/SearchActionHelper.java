package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.web.bean.PagerFilter;

/**
 * Some utility code shared between the searching actions.
 *
 * @since v5.2
 */
public interface SearchActionHelper
{
    /**
     * Store the current pager in the session. The pager handles paging through the issue list.
     *
     * @return the page currently in the session.
     */
    public PagerFilter getPagerFilter();

    /**
     * Store the current pager in the session. The pager handles paging through the issue list.
     * @param tempMax temporary max results per page
     *
     * @return the page currently in the session.
     */
    public PagerFilter getPagerFilter(Integer tempMax);

    /**
     * Restart the pager in the session.
     *
     * @return the new PagerFilter that was created
     */
    public PagerFilter resetPager();

    public void resetPagerTempMax();

    public void resetPagerTempMax(Integer tempMax);
}
