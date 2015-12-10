package com.atlassian.jira.issue.customfields.manager;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.map.CacheObject;

@EventComponent
public class CachedGenericConfigManager implements GenericConfigManager
{
    private final GenericConfigManager delegate;
    private final Cache<Key, CacheObject<Object>> cache;

    public CachedGenericConfigManager(final GenericConfigManager delegate, final CacheManager cacheManager)
    {
        this.delegate = delegate;
        cache = cacheManager.getCache(CachedGenericConfigManager.class.getName() + ".cache",
                new GeneralConfigCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.removeAll();
    }
    
    public void create(final String dataType, final String key, @Nullable final Object obj)
    {
        delegate.create(dataType, key, obj);

        // Remove from cache.. although it shouldn't really exist
        cache.remove(new Key(dataType, key));
    }

    public void update(final String dataType, final String key, @Nullable final Object obj)
    {
        delegate.update(dataType, key, obj);

        // Remove from cache
        cache.remove(new Key(dataType, key));
    }

    public Object retrieve(final String dataType, final String key)
    {
        return cache.get(new Key(dataType, key)).getValue();
    }

    public void remove(final String dataType, final String key)
    {
        delegate.remove(dataType, key);

        // Remove from cache
        cache.remove(new Key(dataType, key));
    }

    private static class Key implements Serializable
    {
        final String dataType;
        final String key;
        transient int hashCode;

        Key(final String dataType, final String key)
        {
            this.dataType = dataType;
            this.key = key;
        }

        @Override
        public int hashCode()
        {
            if (hashCode == 0)
            {
                final int PRIME = 31;
                int result = 1;
                result = PRIME * result + ((dataType == null) ? 0 : dataType.hashCode());
                result = PRIME * result + ((key == null) ? 0 : key.hashCode());
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Key other = (Key) obj;
            if (dataType == null)
            {
                if (other.dataType != null)
                {
                    return false;
                }
            }
            else if (!dataType.equals(other.dataType))
            {
                return false;
            }
            if (key == null)
            {
                if (other.key != null)
                {
                    return false;
                }
            }
            else if (!key.equals(other.key))
            {
                return false;
            }
            return true;
        }
    }

    private class GeneralConfigCacheLoader implements CacheLoader<Key, CacheObject<Object>>
    {
        @Override
        public CacheObject<Object> load(@Nonnull final Key key)
        {
            return CacheObject.wrap(delegate.retrieve(key.dataType, key.key));
        }
    }
}
