/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.cluster.Node;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableSet;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * This is a very basic cache that stores permissions
 * <p/>
 * When constructed, or when you call refresh() - it will find and cache all permissions
 */
public class GlobalPermissionsCache
{
    private static final Logger log = Logger.getLogger(GlobalPermissionsCache.class);
    private final OfBizDelegator ofBizDelegator;

    // set of all permissions
    private final CachedReference<Set<GlobalPermissionEntry>> permissions;

    /**
     * Create a new permissions cache.
     */
    GlobalPermissionsCache(OfBizDelegator ofBizDelegator, CacheManager cacheManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        if (log.isDebugEnabled())
        {
            log.debug("GlobalPermissionsCache.GlobalPermissionsCache");
        }
        permissions = cacheManager.getCachedReference("com.atlassian.jira.security.GlobalPermissionsCache.permissions",
                new PermissionsSupplier());
        permissions.reset();
    }

    public void clearCache()
    {
        permissions.reset();
    }

    public boolean hasPermission(final GlobalPermissionEntry jiraPermission)
    {
        return permissions.get().contains(jiraPermission);
    }

    public Set<GlobalPermissionEntry> getPermissions()
    {
        return permissions.get();
    }

    /**
     * Get a Collection of permission based on a permissionType
     *
     * @param permissionType must be global permission type
     * @return Collction of Permission objects
     */
    public Collection<GlobalPermissionEntry> getPermissions(final String permissionType)
    {
        final List<GlobalPermissionEntry> matchingPerms = new ArrayList<GlobalPermissionEntry>();
        for (final GlobalPermissionEntry perm : permissions.get())
        {
            if (perm.getPermissionKey().equals(permissionType))
            {
                matchingPerms.add(perm);
            }
        }
        return matchingPerms;
    }

    private class PermissionsSupplier implements Supplier<Set<GlobalPermissionEntry>>
    {
        @Override
        public Set<GlobalPermissionEntry> get()
        {
            Set<GlobalPermissionEntry> permissions = new HashSet<GlobalPermissionEntry>();

            final Collection<GenericValue> allPermissions = ofBizDelegator.findAll("GlobalPermissionEntry");
            for (final GenericValue permissionGV : allPermissions)
            {
                final GlobalPermissionEntry permEntry = new GlobalPermissionEntry(permissionGV);
                boolean added = permissions.add(permEntry);
                if (!added)
                {
                    log.warn("Could not add permission " + permEntry + " - it already existed?");
                }
            }
            return ImmutableSet.copyOf(permissions);
        }
    }
}
