package com.atlassian.jira.issue.search.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.Query;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

/**
 * Manages storing search requests ("session" search) in user properties.
 *
 * @since v5.2
 */
@InjectableComponent
public class SearchPropertiesManager
{
    public static final String FILTER_ID_KEY = "user.search.filter.id";
    public static final String JQL_KEY = "user.search.jql";

    private static final Logger log = Logger.getLogger(
            SearchPropertiesManager.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchRequestService searchRequestService;
    private final SearchService searchService;
    private final UserPropertyManager userPropertyManager;

    public SearchPropertiesManager(
            JiraAuthenticationContext jiraAuthenticationContext,
            SearchRequestService searchRequestService,
            SearchService searchService,
            UserPropertyManager userPropertyManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchRequestService = searchRequestService;
        this.searchService = searchService;
        this.userPropertyManager = userPropertyManager;
    }

    /**
     * @return The {@link SearchRequest} stored in the current user's properties
     * or {@code null} if one hasn't been stored or the user isn't logged in.
     */
    public SearchRequest getSearchRequest()
    {
        SearchRequest searchRequest = null;
        if (getPropertySet() != null)
        {
            Long filterId = getFilterId();
            if (filterId != null)
            {
                User user = jiraAuthenticationContext.getLoggedInUser();
                searchRequest = searchRequestService.getFilter(
                        new JiraServiceContextImpl(user), filterId);
            }

            Query query = getJQLQuery();
            if (query != null)
            {
                if (searchRequest != null)
                {
                    searchRequest.setQuery(query);
                }
                else
                {
                    searchRequest = new SearchRequest(query);
                }
            }
        }

        return searchRequest;
    }

    /**
     * Store a search request in the current user's preferences.
     * <p/>
     * Does nothing if the user isn't logged in.
     *
     * @param searchRequest The search request to store.
     */
    public void setSearchRequest(final SearchRequest searchRequest)
    {
        PropertySet propertySet = getPropertySet();
        if (propertySet == null)
        {
            return;
        }

        try
        {
            if (searchRequest != null)
            {
                // The filter ID is stored as a string to support null values.
                Long filterId = searchRequest.getId();
                propertySet.setText(FILTER_ID_KEY,
                        filterId != null ? filterId.toString() : null);

                String JQL = null;
                if (filterId == null || searchRequest.isModified())
                {
                    JQL = searchService.getGeneratedJqlString(
                            searchRequest.getQuery());
                }

                propertySet.setText(JQL_KEY, JQL);
            }
            else
            {
                // Clear the preferences when searchRequest is null.
                // We use a caching property set, so it is much quicker to read than store these.
                if (propertySet.getText(FILTER_ID_KEY) != null)
                {
                    propertySet.setText(FILTER_ID_KEY, null);
                }
                if (propertySet.getText(JQL_KEY) != null)
                {
                    propertySet.setText(JQL_KEY, null);
                }
            }
        }
        catch (PropertyException e)
        {
            log.warn("Couldn't store a search request in user preferences.");
        }
    }

    private Long getFilterId()
    {
        String filterId = null;
        try
        {
            filterId = getPropertySet().getText(FILTER_ID_KEY);
        }
        catch (PropertyException e)
        {
            log.warn("Couldn't retrieve a filter ID from user properties.");
        }

        if (filterId != null)
        {
            try
            {
                return Long.parseLong(filterId);
            }
            catch (NumberFormatException e)
            {
                log.warn("Invalid filter ID in user preferences: " + filterId);
            }
        }

        return null;
    }

    private Query getJQLQuery()
    {
        String JQL = null;
        try
        {
            JQL = getPropertySet().getText(JQL_KEY);
        }
        catch (PropertyException e)
        {
            log.warn("Couldn't retrieve JQL from user properties.");
        }

        if (JQL != null && !JQL.isEmpty())
        {
            User user = jiraAuthenticationContext.getLoggedInUser();
            SearchService.ParseResult parseResult =
                    searchService.parseQuery(user, JQL);

            if (parseResult.isValid())
            {
                return parseResult.getQuery();
            }
        }

        return null;
    }

    private PropertySet getPropertySet()
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        return user != null ? userPropertyManager.getPropertySet(user) : null;
    }
}