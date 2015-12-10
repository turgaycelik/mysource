package com.atlassian.jira.license;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.jira.entity.EntityEngine;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestLicenseRoleGroupsCache
{
    private static final LicenseRoleId LICENCE_ROLE_ID = new LicenseRoleId("Role 1");

    private static final String GROUP_1 = "Group 1";
    private static final String GROUP_2 = "Group 1";

    private static final Collection<String> GROUPS = ImmutableSet.of(GROUP_1, GROUP_2);

    @Mock
    private EntityEngine entityEngine;
    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache<String, Collection<String>> licenseRoleGroupsInnerCache;

    @Before
    public void setUp()
    {
        when(cacheManager.getCache(eq(LicenseRoleGroupsCache.class.getName() + ".licenseRoleGroups"), isA(CacheLoader.class))).thenReturn(licenseRoleGroupsInnerCache);
    }

    @Test
    public void getLicenseRoleGroupsRetrievesGroupsFromInnerCache()
    {
        when(licenseRoleGroupsInnerCache.get(LICENCE_ROLE_ID.getName())).thenReturn(GROUPS);

        LicenseRoleGroupsCache licenseRoleGroupsCache = new LicenseRoleGroupsCache(cacheManager, entityEngine);
        final Collection<String> groupsForLicenseRole = licenseRoleGroupsCache.getGroupsFor(LICENCE_ROLE_ID);

        verify(licenseRoleGroupsInnerCache, times(1)).get(LICENCE_ROLE_ID.getName());
        assertThat(groupsForLicenseRole, equalTo(GROUPS));
    }

    @Test
    public void invalidateCacheEntryRemovesLicenseRoleFromCache()
    {
        LicenseRoleGroupsCache licenseRoleGroupsCache = new LicenseRoleGroupsCache(cacheManager, entityEngine);
        licenseRoleGroupsCache.invalidateCacheEntry(LICENCE_ROLE_ID);
        verify(licenseRoleGroupsInnerCache, times(1)).remove(LICENCE_ROLE_ID.getName());
    }
}
