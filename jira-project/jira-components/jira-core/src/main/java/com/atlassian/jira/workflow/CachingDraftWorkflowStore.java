package com.atlassian.jira.workflow;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.map.CacheObject;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.lang.StringUtils;

/**
 * Provides a caching implementation of the {@link DraftWorkflowStore}.
 *
 * @since v3.13
 */
@EventComponent
public class CachingDraftWorkflowStore implements DraftWorkflowStore
{
    private final DraftWorkflowStore delegate;
    private WorkflowManager workflowManager;
    private final Cache<String, CacheObject<String>> draftWorkflowCache;

    public CachingDraftWorkflowStore(final DraftWorkflowStore delegate, CacheManager cacheManager)
    {
        this.delegate = delegate;
        draftWorkflowCache = cacheManager.getCache(CachingDraftWorkflowStore.class.getName() + ".draftWorkflowCache",
                new DraftWorkflowCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        draftWorkflowCache.removeAll();
    }

    public JiraWorkflow getDraftWorkflow(final String parentWorkflowName) throws DataAccessException
    {
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not get a draft workflow for a parent workflow name of null.");
        }
        CacheObject<String> cacheObject = draftWorkflowCache.get(parentWorkflowName);
        if (cacheObject.equals(CacheObject.NULL_INSTANCE))
        {
            return null;
        }
        else
        {
            return getJiraDraftWorkflow(parentWorkflowName, cacheObject.getValue());
        }
    }

    public JiraWorkflow createDraftWorkflow(final ApplicationUser author, final JiraWorkflow parentWorkflow)
            throws DataAccessException, IllegalStateException, IllegalArgumentException
    {
        final JiraWorkflow draftWorkflow = delegate.createDraftWorkflow(author, parentWorkflow);
        draftWorkflowCache.remove(parentWorkflow.getName());

        return draftWorkflow;
    }

    public boolean deleteDraftWorkflow(final String parentWorkflowName)
            throws DataAccessException, IllegalArgumentException
    {
        final boolean deleted;
        try
        {
            deleted = delegate.deleteDraftWorkflow(parentWorkflowName);
        }
        finally
        {
            draftWorkflowCache.remove(parentWorkflowName);
        }

        return deleted;
    }

    public JiraWorkflow updateDraftWorkflow(final ApplicationUser user, final String parentWorkflowName, final JiraWorkflow workflow)
            throws DataAccessException
    {
        try
        {
            final JiraWorkflow updatedWorkflow = delegate.updateDraftWorkflow(user, parentWorkflowName, workflow);
            return updatedWorkflow;
        }
        finally
        {
            draftWorkflowCache.remove(parentWorkflowName);
        }
    }

    public JiraWorkflow updateDraftWorkflowWithoutAudit(final String parentWorkflowName, final JiraWorkflow workflow)
            throws DataAccessException
    {
        try
        {
            final JiraWorkflow updatedWorkflow = delegate.updateDraftWorkflowWithoutAudit(parentWorkflowName, workflow);
            return updatedWorkflow;
        }
        finally
        {
            draftWorkflowCache.remove(parentWorkflowName);
        }
    }

    WorkflowManager getWorkflowManager()
    {
        if (workflowManager == null)
        {
            workflowManager = ComponentAccessor.getWorkflowManager();
        }
        return workflowManager;
    }

    WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
    {
        return WorkflowUtil.convertXMLtoWorkflowDescriptor(parentWorkflowXML);
    }

    String convertDescriptorToXML(final WorkflowDescriptor descriptor)
    {
        return WorkflowUtil.convertDescriptorToXML(descriptor);
    }

    private JiraWorkflow getJiraDraftWorkflow(final String name, final String workflowDescriptorXML)
    {
        try
        {
            return new JiraDraftWorkflow(name, getWorkflowManager(), convertXMLtoWorkflowDescriptor(workflowDescriptorXML));
        }
        catch (final FactoryException e)
        {
            throw new RuntimeException(e);
        }
    }

    private class DraftWorkflowCacheLoader implements CacheLoader<String, CacheObject<String>>
    {
        @Override
        public CacheObject<String> load(@Nonnull final String parentWorkflowName)
        {
            //if a draft workflow is not in the cache, try to get it from the DB.
            final JiraWorkflow draftWorkflow = delegate.getDraftWorkflow(parentWorkflowName);
            if (draftWorkflow == null)
            {
                // Lets cache the fact that there is no Draft
                return CacheObject.NULL();
            }
            else
            {
                //lets lazy load the cache with the draft Workflow.
                return CacheObject.wrap(convertDescriptorToXML(draftWorkflow.getDescriptor()));
            }
        }
    }
}
