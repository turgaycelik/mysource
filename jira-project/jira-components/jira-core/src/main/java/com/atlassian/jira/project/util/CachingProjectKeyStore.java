package com.atlassian.jira.project.util;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;

@EventComponent
public class CachingProjectKeyStore implements ProjectKeyStore
{
    private final ProjectKeyStore delegateProjectKeyStore;
    private final CachedReference<ProjectKeyCache> cache;

    public CachingProjectKeyStore(ProjectKeyStore delegateProjectKeyStore, CacheManager cacheManager
    )
    {
        this.delegateProjectKeyStore = delegateProjectKeyStore;
        this.cache = cacheManager.getCachedReference(CachingProjectKeyStore.class.getName() + ".cache",
                new ProjectKeyCacheSupplier());

        refresh();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    @Nullable
    @Override
    public Long getProjectId(final String key)
    {
        return cache.get().getProjectId(key);
    }

    @Override
    public void addProjectKey(final Long projectId, final String projectKey)
    {
        delegateProjectKeyStore.addProjectKey(projectId, projectKey);
        refresh();
    }

    @Override
    public void deleteProjectKeys(final Long projectId)
    {
        delegateProjectKeyStore.deleteProjectKeys(projectId);
        refresh();
    }

    @Nonnull
    @Override
    public Map<String, Long> getAllProjectKeys()
    {
        return cache.get().getAllProjectKeys();
    }

    @Nullable
    @Override
    public Long getProjectIdByKeyIgnoreCase(final String projectKey)
    {
        return cache.get().getProjectIdByKeyIgnoreCase(projectKey);
    }

    @Nonnull
    @Override
    public Set<String> getProjectKeys(final Long projectId)
    {
        return cache.get().getProjectKeys(projectId);
    }

    @Override
    public void refresh()
    {
        cache.reset();
    }

    private class ProjectKeyCacheSupplier implements com.atlassian.cache.Supplier<ProjectKeyCache>
    {
        @Override
        public ProjectKeyCache get()
        {
            return new ProjectKeyCache(delegateProjectKeyStore);
        }
    }
}
