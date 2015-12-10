package com.atlassian.jira.sharing;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Cache is key on entityId:type. This is very broken, but no more broken than and
 * of the other caches.
 *
 * @since v3.13
 */
@EventComponent
public class CachingSharePermissionStore implements SharePermissionStore, Startable
{
    private final SharePermissionStore delegateStore;

    /**
     * The actual cache. Randomly picked these numbers.
     */
    private Cache<Key, SharePermissions> cache;

    public CachingSharePermissionStore(final SharePermissionStore delegateStore, final CacheManager cacheManager)
    {
        Assertions.notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
        cache = cacheManager.getCache(CachingSharePermissionStore.class.getName() + ".cache",
                new SharePermissionsCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maxEntries(3000).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.removeAll();
    }

    public SharePermissions getSharePermissions(final SharedEntity entity)
    {
        validate(entity);

        SharePermissions sharePermissions = cache.get(createKey(entity));

        return sharePermissions;
    }

    public int deleteSharePermissions(final SharedEntity entity)
    {
        validate(entity);
        try
        {
            return delegateStore.deleteSharePermissions(entity);
        }
        finally
        {
            cache.remove(createKey(entity));
        }
    }

    public int deleteSharePermissionsLike(final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("permission.type", permission.getType());
        try
        {
            return delegateStore.deleteSharePermissionsLike(permission);
        }
        finally
        {
            cache.removeAll();
        }
    }

    public SharePermissions storeSharePermissions(final SharedEntity entity)
    {
        validate(entity);
        Assertions.notNull("permissions", entity.getPermissions());

        try
        {
            SharePermissions sharePermissions = delegateStore.storeSharePermissions(entity);
            if (sharePermissions == null)
            {
                sharePermissions = SharePermissions.PRIVATE;
            }
            return sharePermissions;
        }
        finally
        {
            cache.remove(createKey(entity));
        }

    }

    private void validate(final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity.id", entity.getId());
        Assertions.notNull("entity.entityType", entity.getEntityType());
    }

    @Override
    public void start() throws Exception
    {
    }

    private static Key createKey(final SharedEntity entity)
    {
        return new Key(entity.getId().longValue(), entity);
    }

    /**
     * Key object for the cache.
     */
    private static class Key implements Serializable
    {
        private final long id;
        private final SharedEntity.TypeDescriptor<SharedEntity> entityType;

        public Key(final long id, final SharedEntity entity)
        {
            Assertions.notNull("id", id);
            Assertions.notNull("entity", entity);

            this.id = id;
            this.entityType = entity.getEntityType();
        }

        ///CLOVER:OFF
        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final Key key = (Key) o;

            if (id != key.id)
            {
                return false;
            }
            if (!entityType.equals(key.entityType))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = (int) (id ^ (id >>> 32));
            result = 31 * result + entityType.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return id + ":" + entityType;
        }
        ///CLOVER:ON
    }

    private class SharePermissionsCacheLoader implements CacheLoader<Key, SharePermissions>
    {
        @Override
        public SharePermissions load(final Key key)
        {
            SharedEntity identifier = new SharedEntity.Identifier(key.id, key.entityType, (ApplicationUser) null);
            SharePermissions sharePermissions = CachingSharePermissionStore.this.delegateStore.getSharePermissions(identifier);
            if (sharePermissions == null)
            {
                sharePermissions = SharePermissions.PRIVATE;
            }
            return sharePermissions;
        }
    }
}
