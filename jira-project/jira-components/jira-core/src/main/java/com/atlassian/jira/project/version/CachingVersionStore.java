/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.project.version;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;

import org.ofbiz.core.entity.GenericValue;

/**
 * A caching implementation of the VersionStore that relies on a delegate to do the DB operations.
 */
@EventComponent
public class CachingVersionStore implements VersionStore
{

    private final VersionStore delegate;

    private final Cache<Long, Optional<GenericValue>> versionById;
    private final CachedReference<List<GenericValue>> allVersions;
    private final Cache<String, List<GenericValue>> versionsByName;
    private final Cache<Long, List<GenericValue>> versionsByProjectId;

    public CachingVersionStore(final VersionStore delegate, final CacheManager cacheManager)
    {
        this.delegate = delegate;
        versionById = cacheManager.getCache(getCacheReferenceName("versionById"), new CacheLoader<Long, Optional<GenericValue>>()
        {

            @Nonnull
            @Override
            public Optional<GenericValue> load(@Nonnull final Long id)
            {
                return Optional.fromNullable(delegate.getVersion(id));
            }

        });
        allVersions = cacheManager.getCachedReference(getCacheReferenceName("allVersions"), new Supplier<List<GenericValue>>()
        {

            @Override
            public List<GenericValue> get()
            {
                return ImmutableList.copyOf(delegate.getAllVersions());
            }

        });
        versionsByName = cacheManager.getCache(getCacheReferenceName("versionsByName"), new CacheLoader<String, List<GenericValue>>()
        {
            @Nonnull
            @Override
            public List<GenericValue> load(@Nonnull final String name)
            {
                return ImmutableList.copyOf(delegate.getVersionsByName(name));
            }

        });
        versionsByProjectId = cacheManager.getCache(getCacheReferenceName("versionsByProjectId"), new CacheLoader<Long, List<GenericValue>>()
        {
            @Nonnull
            @Override
            public List<GenericValue> load(@Nonnull final Long projectId)
            {
                return ImmutableList.copyOf(delegate.getVersionsByProject(projectId));
            }

        });
    }

    private String getCacheReferenceName(String name)
    {
        return CachingVersionStore.class.getCanonicalName() + ".cache." + name;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refreshCache(Collections.<Long>emptyList());
    }

    public List<GenericValue> getAllVersions()
    {
        return allVersions.get();
    }

    public GenericValue getVersion(final Long id)
    {
        return id != null ? versionById.get(id).orNull() : null;
    }

    public GenericValue createVersion(final Map<String, Object> versionParams)
    {
        final GenericValue version = delegate.createVersion(versionParams);
        refreshCache(Collections.singletonList(version.getLong("id")));
        return version;
    }

    public void storeVersion(final Version version)
    {
        delegate.storeVersion(version);
        refreshCache(Collections.singletonList(version.getId()));
    }

    public void storeVersions(final Collection<Version> versions)
    {
        delegate.storeVersions(versions);
        List<Long> versionIds = new LinkedList<Long>();
        for (Version version : versions)
        {
            if (version != null)
            {
                versionIds.add(version.getId());
            }
        }
        refreshCache(versionIds);
    }

    public void deleteVersion(final GenericValue versionGV)
    {
        long versionId = versionGV.getLong("id");
        delegate.deleteVersion(versionGV);
        refreshCache(Collections.singletonList(versionId));
    }


    @Override
    public List<GenericValue> getVersionsByName(String name)
    {
        List<GenericValue> result = null;
        if (name != null)
        {
            result = versionsByName.get(name);
        }
        return result != null ?  result : Collections.<GenericValue> emptyList();
    }

    @Override
    public List<GenericValue> getVersionsByProject(Long projectId)
    {
        List<GenericValue> result = null;
        if (projectId != null)
        {
            result = versionsByProjectId.get(projectId);
        }
        return result != null ?  result : Collections.<GenericValue> emptyList();
    }

    private void refreshCache(Collection<Long> versionIds)
    {
        if (!versionIds.isEmpty())
        {
            for (long versionId : versionIds)
            {
                versionById.remove(versionId);
            }
        }
        else
        {
            versionById.removeAll();
        }
        allVersions.reset();
        versionsByName.removeAll();
        versionsByProjectId.removeAll();
    }

}
