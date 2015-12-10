/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//This class has been updated to reflect the changes made in permissions from a project level to a scheme level
public class TestGlobalPermissionsCache
{
    @Test
    public void testHasGetPermission()
    {
        final MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
        GlobalPermissionsCache globalPermissionsCache = new GlobalPermissionsCache(mockOfBizDelegator, new MemoryCacheManager());
        GenericValue perm1 = mockOfBizDelegator.createValue("GlobalPermissionEntry", FieldMap.build("permission", "USE", "group_id", "Test Group"));
        // First access
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));

        globalPermissionsCache.clearCache();
        // First access
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));

        GenericValue perm2 = mockOfBizDelegator.createValue("GlobalPermissionEntry", FieldMap.build("permission", "ADMIN", "group_id", "Test Group"));
        // Stale cache (because values were put directly into the 'db')
        assertFalse(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm2)));

        globalPermissionsCache.clearCache();
        // First access
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm2)));
    }

    @Test
    public void testRemovePermission()
    {
        final MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
        GlobalPermissionsCache globalPermissionsCache = new GlobalPermissionsCache(mockOfBizDelegator, new MemoryCacheManager());
        GenericValue perm1 = mockOfBizDelegator.makeValue("GlobalPermissionEntry", FieldMap.build("permission", "USE", "group_id", "Test Group"));
        assertFalse(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));
        perm1 = mockOfBizDelegator.createValue("GlobalPermissionEntry", FieldMap.build("permission", "USE", "group_id", "Test Group"));
        assertFalse(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));

        globalPermissionsCache.clearCache();
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));

        GenericValue perm2 = mockOfBizDelegator.createValue("GlobalPermissionEntry", FieldMap.build("permission", "ADMIN", "group_id", "Test Group"));
        assertFalse(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm2)));

        globalPermissionsCache.clearCache();
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm1)));
        assertTrue(globalPermissionsCache.hasPermission(new GlobalPermissionEntry(perm2)));
    }
}
