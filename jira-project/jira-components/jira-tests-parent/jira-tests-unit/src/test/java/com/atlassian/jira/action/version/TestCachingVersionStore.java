/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.action.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.CachingVersionStore;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionStore;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCachingVersionStore
{
    private MemoryVersionStore underlyingStore;
    private CachingVersionStore cachingVersionStore;

    @Before
    public void setUp() throws Exception
    {
        underlyingStore = new MemoryVersionStore();
        cachingVersionStore = new CachingVersionStore(underlyingStore, new MemoryCacheManager());
    }

    @Test
    public void testGetVersion() throws Exception
    {
        Version version1 = new MockVersion(1, "v1.0");
        Version version2 = new MockVersion(2, "v1.5");
        Version version3 = new MockVersion(3, "v2.0");

        cachingVersionStore.storeVersion(version1);
        cachingVersionStore.storeVersion(version2);

        assertEquals(2, cachingVersionStore.getAllVersions().size());
        assertEquals(version1.getGenericValue(), cachingVersionStore.getAllVersions().get(0));
        assertEquals(version1.getGenericValue(), cachingVersionStore.getVersion(new Long(1)));
        assertEquals(version2.getGenericValue(), cachingVersionStore.getAllVersions().get(1));
        assertEquals(version2.getGenericValue(), cachingVersionStore.getVersion(new Long(2)));

        cachingVersionStore.storeVersion(version3);

        assertEquals(3, cachingVersionStore.getAllVersions().size());
        assertEquals(version1.getGenericValue(), cachingVersionStore.getAllVersions().get(0));
        assertEquals(version1.getGenericValue(), cachingVersionStore.getVersion(new Long(1)));
        assertEquals(version2.getGenericValue(), cachingVersionStore.getAllVersions().get(1));
        assertEquals(version2.getGenericValue(), cachingVersionStore.getVersion(new Long(2)));
        assertEquals(version3.getGenericValue(), cachingVersionStore.getAllVersions().get(2));
        assertEquals(version3.getGenericValue(), cachingVersionStore.getVersion(new Long(3)));
    }

    @Test
    public void testCreateAndDeleteModifiesUnderlyingVersionCorrectly()
    {
        Map versionParams = EasyMap.build("name", "version1", "id", new Long(1));
        Map versionParams2 = EasyMap.build("name", "version2", "id", new Long(2));
        GenericValue version = new MockGenericValue("Version", versionParams);
        GenericValue version2 = new MockGenericValue("Version", versionParams2);

        cachingVersionStore.createVersion(versionParams);
        cachingVersionStore.createVersion(versionParams2);
        //assert created
        assertEquals(2, underlyingStore.getAllVersions().size());
        assertTrue(underlyingStore.getAllVersions().contains(version));
        assertTrue(underlyingStore.getAllVersions().contains(version2));

        assertEquals(underlyingStore.getAllVersions(), cachingVersionStore.getAllVersions());

        assertGetVersion(version, new Long(1));
        assertGetVersion(version2, new Long(2));
        assertGetVersion(null, new Long(3));

        cachingVersionStore.deleteVersion(version);
        cachingVersionStore.deleteVersion(version2);

        assertTrue(underlyingStore.getAllVersions().isEmpty());
        assertEquals(underlyingStore.getAllVersions(), cachingVersionStore.getAllVersions());
        assertGetVersion(null, new Long(1));
        assertGetVersion(null, new Long(2));

    }

    private void assertGetVersion(GenericValue version, Long id)
    {
        assertEquals(version, underlyingStore.getVersion(id));
        assertEquals(version, cachingVersionStore.getVersion(id));
    }

    private static class MemoryVersionStore implements VersionStore
    {

        private List versions = new ArrayList();

        public List<GenericValue> getAllVersions()
        {
            List versionGVs = new ArrayList(versions.size());
            for (Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                GenericValue version = (GenericValue) iterator.next();
                versionGVs.add(version);
            }
            return versionGVs;
        }

        public GenericValue createVersion(Map versionParams)
        {
            GenericValue versionGV = new MockGenericValue("Version", versionParams);
            versions.add(versionGV);
            return versionGV;
        }

        public void storeVersion(Version version)
        {
            versions.remove(version.getGenericValue());
            versions.add(version.getGenericValue());
        }

        public void storeVersions(final Collection<Version> versions)
        {
            for (Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                Version version = (Version) iterator.next();
                if (version != null)
                {
                    storeVersion(version);
                }
            }
        }

        public GenericValue getVersion(Long id)
        {
            for (Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                GenericValue version = (GenericValue) iterator.next();
                if (id.equals(version.getLong("id")))
                    return version;
            }
            return null;
        }

        public void deleteVersion(GenericValue versionGV)
        {
            versions.remove(versionGV);
        }

        @Override
        public List<GenericValue> getVersionsByName(String name)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GenericValue> getVersionsByProject(Long projectId)
        {
            throw new UnsupportedOperationException();
        }
    }
}
