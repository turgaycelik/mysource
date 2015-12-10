package com.atlassian.jira.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.map.CacheObject;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * Used to cache workflowDescriptors in JIRA. This caches {@link ImmutableWorkflowDescriptor}s.
 * These objects are very heavy weight and ideally we would not cache them, but it is the only way to
 * quickly give JIRA access to workflow objects. This is because the safe thing to cache is the workflow XML string but
 * converting this to an object graph will be expensive.  Also please note that the implementation of
 * {@link ImmutableWorkflowDescriptor} cannot guarantee 100% immutability.
 * <p/>
 * This is essentially replacing the store in the {@link com.atlassian.jira.workflow.JiraWorkflowFactory}, but it adds
 * some more concurrency controls to ensure consistency with the underlying store (such as the
 * {@link com.atlassian.jira.workflow.OfBizWorkflowDescriptorStore})
 *
 * @since v3.13
 */
@EventComponent
public class CachingWorkflowDescriptorStore implements WorkflowDescriptorStore
{
    private final Cache<String, CacheObject<ImmutableWorkflowDescriptor>> workflowCache;
    private final CachedReference<String[]> allNamesCache;
    private final WorkflowDescriptorStore delegate;

    public CachingWorkflowDescriptorStore(final WorkflowDescriptorStore delegate, final CacheManager cacheManager)
    {
        this.delegate = delegate;

        workflowCache = cacheManager.getCache(CachingWorkflowDescriptorStore.class.getName() + ".workflowCache",
                new WorkflowCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
        allNamesCache = cacheManager.getCachedReference(CachingWorkflowDescriptorStore.class.getName() + ".allNamesCache",
                new AllNamesSupplier());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        workflowCache.removeAll();
        allNamesCache.reset();
    }

    public ImmutableWorkflowDescriptor getWorkflow(final String name) throws FactoryException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }
        return workflowCache.get(name).getValue();
    }

    public boolean removeWorkflow(final String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }

        boolean deleted;
        try
        {
            deleted = delegate.removeWorkflow(name);
        }
        finally
        {
            workflowCache.remove(name);
            allNamesCache.reset();
        }
        return deleted;
    }

    public boolean saveWorkflow(final String name, final WorkflowDescriptor workflowDescriptor, final boolean replace) throws DataAccessException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name may not be null!");
        }
        if (workflowDescriptor == null)
        {
            throw new IllegalArgumentException("workflowDescriptor may not be null!");
        }
        try
        {
            return delegate.saveWorkflow(name, workflowDescriptor, replace);
        }
        finally
        {
            workflowCache.remove(name);
            allNamesCache.reset();
        }
    }

    public String[] getWorkflowNames()
    {
        final String[] names = allNamesCache.get();
        return Arrays.copyOf(names, names.length);
    }

    public List<JiraWorkflowDTO> getAllJiraWorkflowDTOs()
    {
        return delegate.getAllJiraWorkflowDTOs();
    }

    private class AllNamesSupplier implements Supplier<String[]>
    {
        @Override
        public String[] get()
        {
            return delegate.getWorkflowNames();
        }
    }

    private class WorkflowCacheLoader implements CacheLoader<String,CacheObject<ImmutableWorkflowDescriptor>>
    {
        @Override
        public CacheObject<ImmutableWorkflowDescriptor> load(@Nonnull final String name)
        {
            try
            {
                return CacheObject.wrap(delegate.getWorkflow(name));
            }
            catch (FactoryException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
