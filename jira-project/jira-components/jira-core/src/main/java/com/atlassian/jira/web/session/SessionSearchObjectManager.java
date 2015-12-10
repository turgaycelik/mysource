package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.web.bean.PagerFilter;

/**
 * Provides a common access point for the setting and getting of current search related objects within the current session.
 * <p/>
 * Candidates for this are objects which are related to searching. For example: {@link SearchRequest}, {@link NextPreviousPager},
 * {@link PagerFilter}, or the selected issue.
 *
 * @since v4.2
 */
public interface SessionSearchObjectManager<T>
{

    /**
     * @return the current object for the current user session-wide. Null signifies that there is no current object.
     */
    T getCurrentObject();

    /**
     * Associates the specified object to the current user's session.
     *
     * @param object the object
     */
    void setCurrentObject(T object);
}
