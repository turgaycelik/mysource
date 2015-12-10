package com.atlassian.jira.project.util;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import org.apache.log4j.Logger;

public class ProjectKeyCache
{
    private static final Logger log = Logger.getLogger(ProjectKeyCache.class);

    private ProjectKeyStore delegateProjectKeyStore;
    private ImmutableMap<String, Long> projectsByKey;
    private ImmutableSetMultimap<Long, String> projectKeys;
    private ImmutableSortedMap<String, Long> projectsByKeyIgnoreCase;

    public ProjectKeyCache(ProjectKeyStore delegateProjectKeyStore)
    {
        this.delegateProjectKeyStore = delegateProjectKeyStore;

        init();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        init();
    }

    @Nullable
    public Long getProjectId(final String key)
    {
        return projectsByKey.get(key);
    }

    @Nonnull
    public Map<String, Long> getAllProjectKeys()
    {
        return projectsByKey;
    }

    @Nullable
    public Long getProjectIdByKeyIgnoreCase(final String projectKey)
    {
        return projectsByKeyIgnoreCase.get(projectKey);
    }

    @Nonnull
    public Set<String> getProjectKeys(final Long projectId)
    {
        return projectKeys.get(projectId);
    }

    protected void init()
    {
        if (log.isDebugEnabled())
            log.debug("CachingProjectKeyStore.refresh");

        long start = System.currentTimeMillis();
        UtilTimerStack.push("CachingProjectKeyStore.refresh");

        try
        {
            final Map<String, Long> tmpByKey = Maps.newLinkedHashMap();
            final SetMultimap<Long, String> tmpProjectKeys = HashMultimap.create();
            for (Map.Entry<String, Long> projectKey : delegateProjectKeyStore.getAllProjectKeys().entrySet())
            {
                tmpByKey.put(projectKey.getKey(), projectKey.getValue());
                tmpProjectKeys.put(projectKey.getValue(), projectKey.getKey());
            }

            projectsByKey = ImmutableMap.copyOf(tmpByKey);
            projectKeys = ImmutableSetMultimap.copyOf(tmpProjectKeys);
            projectsByKeyIgnoreCase = ImmutableSortedMap.copyOf(projectsByKey, String.CASE_INSENSITIVE_ORDER);
        }
        finally
        {
            UtilTimerStack.pop("CachingProjectKeyStore.refresh");
        }

        if (log.isDebugEnabled())
            log.debug("CachingProjectKeyStore.refresh took " + (System.currentTimeMillis() - start));
    }
}
