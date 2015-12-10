package com.atlassian.jira.issue.label;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;

/**
 * Caching implementation of the Label store, that caches labels in a weakhashmap based on the issue/field combination.
 *
 * @since v4.2
 */
@EventComponent
public class CachingLabelStore implements LabelStore, Startable
{
    private final OfBizLabelStore delegateStore;
    private Cache<CacheKey, Set<Label>> cache;

    public CachingLabelStore(final OfBizLabelStore delegateStore, CacheManager cacheManager)
    {
        this.delegateStore = delegateStore;
        cache = cacheManager.getCache(CachingLabelStore.class.getName() + ".cache",
                new LabelCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maxEntries(1000).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.removeAll();
    }

    public Set<Label> getLabels(final Long issueId, final Long customFieldId)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        return cache.get(key);
    }

    public Set<Label> setLabels(final Long issueId, final Long customFieldId, final Set<String> labels)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        try
        {
            return delegateStore.setLabels(issueId, customFieldId, labels);
        }
        finally
        {
            cache.remove(key);
        }
    }

    public Label addLabel(final Long issueId, final Long customFieldId, final String label)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        try
        {
            return delegateStore.addLabel(issueId, customFieldId, label);
        }
        finally
        {
            cache.remove(key);
        }
    }

    public void removeLabel(final Long labelId, final Long issueId, final Long customFieldId)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);

        try
        {
            delegateStore.removeLabel(labelId, issueId, customFieldId);
            //clear the cache so that the next read will fix up the cache again.
        }
        finally
        {
            cache.remove(key);
        }
    }

    public Set<Long> removeLabelsForCustomField(final Long customFieldId)
    {
        try
        {
            return delegateStore.removeLabelsForCustomField(customFieldId);
        }
        finally
        {
            //not properly synchronized, but this should only be called very rarely!  Clear the entire
            //cache to ensure no stale entries for custom fields are left.
            cache.removeAll();
        }
    }

    @Override
    public void start() throws Exception
    {
    }

    static final class CacheKey implements Serializable
    {
        private final Long issueId;
        private final Long fieldId;

        CacheKey(final Long issueId, final Long fieldId)
        {
            this.issueId = issueId;
            this.fieldId = fieldId;
        }

        public Long getFieldId()
        {
            return fieldId;
        }

        public Long getIssueId()
        {
            return issueId;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final CacheKey cacheKey = (CacheKey) o;

            if (fieldId != null ? !fieldId.equals(cacheKey.fieldId) : cacheKey.fieldId != null)
            {
                return false;
            }
            if (!issueId.equals(cacheKey.issueId))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = issueId.hashCode();
            result = 31 * result + (fieldId != null ? fieldId.hashCode() : 0);
            return result;
        }
    }

    private class LabelCacheLoader implements CacheLoader<CacheKey, Set<Label>>
    {
        @Override
        public Set<Label> load(final CacheKey key)
        {
            return CachingLabelStore.this.delegateStore.getLabels(key.getIssueId(), key.getFieldId());
        }
    }
}
