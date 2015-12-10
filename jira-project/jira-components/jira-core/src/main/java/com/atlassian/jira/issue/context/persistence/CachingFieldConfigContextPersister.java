package com.atlassian.jira.issue.context.persistence;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching decorator for {@link FieldConfigContextPersister}. This corresponds to the <code>configurationcontext</code>
 * table, which is essentially an association table between {@link com.atlassian.jira.issue.fields.CustomField
 * CustomField} and either a {@link ProjectCategory} or a {@link Project}, but not both (in practice it is always a
 * Project). Each association also has {@link FieldConfigScheme} as a property of the association, and this is where
 * things like default values for custom fields are ultimately stored. When both the project and projectCategory are
 * null, then that database row is in fact a special row holding the FieldConfigScheme for the "Global Context".
 * <p/>
 * See <a href="https://extranet.atlassian.com/x/koEPJg">CustomField Configuration - DB Entity Model</a> for a more
 * in-depth explanation of how this all works.
 *
 * @since v5.1
 */
@Internal
@EventComponent
public class CachingFieldConfigContextPersister implements FieldConfigContextPersister, Startable
{
    private static final Logger log = LoggerFactory.getLogger(CachingFieldConfigContextPersister.class);

    /**
     * We are keeping a cache of lower level caches, so we can do sensible invalidation across the cluster.
     * The outer cache is keyed by CustomField id while the inner is keyed by the whole bandana kit and caboodle.
     */
    private final Cache<String, com.google.common.cache.Cache<CacheKey, CacheObject>> cache;

    /**
     * The real FieldConfigContextPersisterImpl.
     */
    private final FieldConfigContextPersister delegate;

    /**
     * Creates a new CachingFieldConfigContextPersister that wraps a new FieldConfigContextPersisterImpl instance.
     *
     * @param delegator the OfBizDelegator
     * @param projectManager the ProjectManager
     * @param treeManager the JiraContextTreeManager
     */
    @SuppressWarnings ("UnusedDeclaration")
    public CachingFieldConfigContextPersister(OfBizDelegator delegator, ProjectManager projectManager, JiraContextTreeManager treeManager, CacheManager cacheManager)
    {
        this.delegate = new FieldConfigContextPersisterImpl(delegator, projectManager, treeManager);
        cache = cacheManager.getCache(CachingFieldConfigContextPersister.class.getName() + ".cache",
                new FieldConfigContextCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).flushable().build());
    }

    /**
     * Registers this CachingFieldConfigContextPersister's cache in the JIRA instrumentation.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception
    {
    }

    /**
     * Clears this CachingFieldConfigContextPersister's cache upon receiving a ClearCacheEvent.
     *
     * @param clearCacheEvent a ClearCacheEvent
     */
    @EventListener
    @SuppressWarnings ("UnusedParameters")
    public void onClearCache(ClearCacheEvent clearCacheEvent)
    {
        invalidateAll();
    }

    //<editor-fold desc="FieldConfigContextPersister methods">
    @Override
    public List<JiraContextNode> getAllContextsForCustomField(String key)
    {
        return delegate.getAllContextsForCustomField(key);
    }

    @Override
    public List<JiraContextNode> getAllContextsForConfigScheme(FieldConfigScheme fieldConfigScheme)
    {
        return delegate.getAllContextsForConfigScheme(fieldConfigScheme);
    }

    @Override
    public void removeContextsForConfigScheme(FieldConfigScheme fieldConfigScheme)
    {
        delegate.removeContextsForConfigScheme(fieldConfigScheme);
        if (fieldConfigScheme != null && fieldConfigScheme.getField() != null)
        {
            cache.remove(fieldConfigScheme.getField().getId());
        }
    }

    @Override
    public void removeContextsForConfigScheme(Long fieldConfigSchemeId)
    {
        delegate.removeContextsForConfigScheme(fieldConfigSchemeId);
        cache.removeAll();
    }

    @Override
    public void removeContextsForProject(final GenericValue project)
    {
        delegate.removeContextsForProject(project);
        cache.removeAll();
    }

    @Override
    public void removeContextsForProject(final Project project)
    {
        delegate.removeContextsForProject(project);
        cache.removeAll();
    }

    @Override
    public void removeContextsForProjectCategory(ProjectCategory projectCategory)
    {
        delegate.removeContextsForProjectCategory(projectCategory);
        cache.removeAll();
    }
    //</editor-fold>

    //<editor-fold desc="BandanaPersister methods">
    @Override
    public Object retrieve(BandanaContext context, String key)
    {
        com.google.common.cache.Cache<CacheKey, CacheObject> innerCache = cache.get(key);
        return innerCache.getUnchecked(new CacheKey((JiraContextNode) context, key)).getValue();
    }

    @Override
    public void store(BandanaContext context, final String customField, Object fieldConfigScheme)
    {
        delegate.store(context, customField, fieldConfigScheme);
        cache.remove(customField);
    }

    @Override
    public void store(Collection<? extends BandanaContext> contexts, final String customField, Object fieldConfigScheme)
    {
        delegate.store(contexts, customField, fieldConfigScheme);
        cache.remove(customField);
    }


    @Override
    public void flushCaches()
    {
        invalidateAll();
    }

    @Override
    public void remove(final BandanaContext context)
    {
        delegate.remove(context);
        cache.removeAll();
    }

    @Override
    public void remove(BandanaContext context, String customField)
    {
        delegate.remove(context, customField);
        cache.remove(customField);
    }
    //</editor-fold>

    /**
     * Clears this instance's cache.
     */
    private void invalidateAll()
    {
        cache.removeAll();
        if (log.isTraceEnabled())
        {
            log.trace("called invalidateAll()", new Throwable());
        }
    }

    /**
     * This cache key is messed up because there are in fact three different types of JiraContextNode, but the database
     * equality is not the same as object equality in Java. At the database level, JIRA uses the output of {@link
     * JiraContextNode#appendToParamsMap(java.util.Map)} to determine equality, which is in contrast to the
     * implementation of the JiraContextNode equals/hashCode methods.
     */
    static final class CacheKey implements Serializable
    {
        /**
         * The context parameters, as given by {@link com.atlassian.jira.issue.context.JiraContextNode#appendToParamsMap(java.util.Map)},
         */
        private final Map<String, Object> contextParams;
        private final int hash;

        /**
         * The custom field id (as a string).
         */
        private final String customField;

        /**
         * This is not really used as a cache key. It's only kept around so it can be passed to the real
         * FieldConfigContextPersister when we need to load a row from the database.
         */
        private final JiraContextNode contextNode;

        CacheKey(JiraContextNode contextNode, String customField)
        {
            this.contextNode = contextNode;
            this.customField = customField;
            this.contextParams = contextNode.appendToParamsMap(null);
            this.hash = 31 * contextParams.hashCode() + (customField != null ? customField.hashCode() : 0);
        }

        @Override
        @SuppressWarnings ("RedundantIfStatement")
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            CacheKey cacheKey = (CacheKey) o;
            if (hash != cacheKey.hash)
            {
                return false;
            }
            if (customField != null ? !customField.equals(cacheKey.customField) : cacheKey.customField != null)
            {
                return false;
            }
            return contextParams.equals(cacheKey.contextParams);
        }

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public String toString()
        {
            return "CacheKey{" + contextParams + "/" + customField + '}';
        }
    }

    /**
     * Matches cache keys by project id.
     */
    private static class ProjectIdMatcher implements Predicate<CacheKey>
    {
        private final Long projectId;

        public ProjectIdMatcher(Long projectId)
        {
            this.projectId = projectId;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            return projectId.equals(key.contextNode.getProjectId());
        }
    }

    /**
     * Matches cache keys by context.
     */
    private static class ContextMatcher implements Predicate<CacheKey>
    {
        private final BandanaContext context;

        public ContextMatcher(BandanaContext context)
        {
            this.context = context;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            return context.equals(key.contextNode);
        }
    }

    /**
     * Matches cache keys by custom field.
     */
    private static class CustomFieldMatcher implements Predicate<CacheKey>
    {
        private final String customField;

        public CustomFieldMatcher(String customField)
        {
            this.customField = customField;
        }

        @Override
        public boolean apply(CacheKey input)
        {
            return Objects.equal(input.customField, customField);
        }
    }

    /**
     * Matches cache keys by project category.
     */
    private class ProjectCategoryMatcher implements Predicate<CacheKey>
    {
        private final ProjectCategory projectCategory;

        public ProjectCategoryMatcher(ProjectCategory projectCategory)
        {
            this.projectCategory = projectCategory;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            Project project = key.contextNode.getProjectObject();
            return project != null && projectCategory.equals(project.getProjectCategoryObject());
        }
    }

    private class FieldConfigContextCacheLoader implements CacheLoader<String, com.google.common.cache.Cache<CacheKey, CacheObject>>
    {
        @Override
        public com.google.common.cache.Cache<CacheKey, CacheObject> load(String key)
        {
            return CacheBuilder.newBuilder().build(new com.google.common.cache.CacheLoader<CacheKey, CacheObject>()
            {
                @Override
                public CacheObject load(CacheKey key) throws Exception
                {
                    return CacheObject.wrap(delegate.retrieve(key.contextNode, key.customField));
                }
            });
        }
    }
}
