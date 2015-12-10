/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

public class PermissionsFilterGeneratorImpl implements PermissionsFilterGenerator
{
    private static final Logger log = Logger.getLogger(PermissionsFilterGeneratorImpl.class);

    private final PermissionQueryFactory permissionQueryFactory;

    public PermissionsFilterGeneratorImpl(final PermissionQueryFactory permissionQueryFactory)
    {
        this.permissionQueryFactory = permissionQueryFactory;
    }

    public Query getQuery(final User searcher)
    {
        try
        {
            UtilTimerStack.push("Permission Query");

            // if we have a cached query, just return that
            Query query = getCache().getQuery(searcher);
            if (query == null)
            {
                query = permissionQueryFactory.getQuery(ApplicationUsers.from(searcher), Permissions.BROWSE);
                getCache().storeQuery(query, searcher);
            }

            return query;
        }
        finally
        {
            UtilTimerStack.pop("Permission Query");
        }
    }

    ///CLOVER:OFF
    PermissionsFilterCache getCache()
    {
        PermissionsFilterCache cache = (PermissionsFilterCache) JiraAuthenticationContextImpl.getRequestCache().get(
            RequestCacheKeys.PERMISSIONS_FILTER_CACHE);

        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new PermissionsFilterCache");
            }
            cache = new PermissionsFilterCache();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.PERMISSIONS_FILTER_CACHE, cache);
        }

        return cache;
    }
    ///CLOVER:ON
}
