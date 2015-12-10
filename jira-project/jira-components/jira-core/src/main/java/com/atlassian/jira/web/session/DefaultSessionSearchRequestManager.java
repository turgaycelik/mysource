package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.SearchPropertiesManager;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.SessionKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to getting and setting {@link SearchRequest} objects in session.
 *
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager()
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 * @since v4.2
 */
@NonInjectableComponent
public class DefaultSessionSearchRequestManager extends AbstractSessionSearchObjectManager<SearchRequest>
        implements SessionSearchRequestManager
{
    private final SearchPropertiesManager searchPropertiesManager;

    public DefaultSessionSearchRequestManager(
            final HttpServletRequest request, final Session session,
            final SearchPropertiesManager searchPropertiesManager)
    {
        super(request, session);
        this.searchPropertiesManager = searchPropertiesManager;
    }

    protected String getLastViewedSessionKey()
    {
        return SessionKeys.SEARCH_REQUEST;
    }

    /**
     * Stores a search request in the current user's session and preferences.
     *
     * @param searchRequest The search request.
     */
    @Override
    public void setCurrentObject(SearchRequest searchRequest)
    {
        super.setCurrentObject(searchRequest);
        searchPropertiesManager.setSearchRequest(searchRequest);
    }

    /**
     * @return the current user's "session" search.
     */
    @Override
    public SearchRequest getCurrentObject()
    {
        SearchRequest searchRequest = super.getCurrentObject();

        if (searchRequest != null)
        {
            return searchRequest;
        }
        else
        {
            return searchPropertiesManager.getSearchRequest();
        }
    }
}