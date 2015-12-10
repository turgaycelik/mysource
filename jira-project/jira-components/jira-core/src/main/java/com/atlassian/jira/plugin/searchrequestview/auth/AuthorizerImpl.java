package com.atlassian.jira.plugin.searchrequestview.auth;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * The one we deploy for production, other impls have unit tests, this one builds an aggregate of those.
 */
public class AuthorizerImpl implements Authorizer
{
    private static final Logger log = Logger.getLogger(AuthorizerImpl.class);

    private final Authorizer delegate;

    public AuthorizerImpl(SearchProvider searchProvider, ApplicationProperties properties, GroupManager groupManager)
    {
        Null.not("searchProvider", searchProvider);
        Null.not("properties", properties);
        Null.not("userManager", groupManager);

        long limit = getResultLimit(properties);
        boolean noLimit = (limit < 0);
        if (noLimit)
        {
            // just setup the simple always good authorizer
            this.delegate = Authorizer.ALWAYS;
        }
        else
        {
            // we will be limiting so setup the full chain of delegates,
            // build delegates backwards so they encapsulate each other
            Authorizer authorizer = new SearchResultSizeAuthorizer(searchProvider, limit, Authorizer.ALWAYS);
            String allowedGroupName = properties.getDefaultBackedString(APKeys.JIRA_SEARCH_VIEWS_MAX_UNLIMITED_GROUP);
            if (StringUtils.isNotBlank(allowedGroupName))
            {
                Group  allowedGroup = groupManager.getGroup(allowedGroupName);
                if (allowedGroup == null)
                {
                    log.error("The group: '" + allowedGroupName + "' specified as the property: '" + APKeys.JIRA_SEARCH_VIEWS_MAX_UNLIMITED_GROUP
                            + "' does not exist. Cannot setup a group to bypass search result filtering");
                }
                else
                {
                    authorizer = new UserAuthorizer(allowedGroup, authorizer, groupManager);
                }
            }
            this.delegate = authorizer;
        }
    }

    @Override
    public Result isSearchRequestAuthorized(User user, SearchRequest searchRequest, SearchRequestParams params)
    {
        return delegate.isSearchRequestAuthorized(user, searchRequest, params);
    }

    Authorizer getDelegate()
    {
        return delegate;
    }

    static long getResultLimit(ApplicationProperties properties)
    {
        String defaultMax = properties.getDefaultBackedString(APKeys.JIRA_SEARCH_VIEWS_MAX_LIMIT);
        try
        {
            if (StringUtils.isBlank(defaultMax))
            {
                return -1;
            }
            return Long.valueOf(defaultMax).intValue();
        }
        catch (NumberFormatException e)
        {
            // Will only get called on startup after admin has manually added a BAD limit. Might as well fail fast!
            throw new IllegalArgumentException("Cannot get search result restriction limit for: '" + defaultMax + "' key=" + APKeys.JIRA_SEARCH_VIEWS_MAX_LIMIT);
        }
    }
}