package com.atlassian.jira.favourites;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Cache is keyed on userkey:type. This is very broken, but no more broken than any
 * of the other caches.
 *
 * @since v3.13
 */
@EventComponent
public class CachingFavouritesStore implements FavouritesStore, Startable
{
    /**
     * The actual cache. Randomly picked these numbers.
     */
    private final Cache<Key, Collection<Long>> favouritesCache;

    private final FavouritesStore delegateStore;

    public CachingFavouritesStore(final FavouritesStore delegateStore, final CacheManager cacheManager)
    {
        this.delegateStore = notNull("delegateStore", delegateStore);
        favouritesCache = cacheManager.getCache(CachingFavouritesStore.class.getName() + ".favouritesCache",
                new KeyCollectionCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maxEntries(1000).build());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        favouritesCache.removeAll();
    }

    public boolean addFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        try
        {
            return delegateStore.addFavourite(notNull("user", user), notNull("entity", entity));
        }
        finally
        {
            flushFavourites(user, entity.getEntityType());
        }
    }

    public boolean removeFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        try
        {
            return delegateStore.removeFavourite(notNull("user", user), notNull("entity", entity));
        }
        finally
        {
            flushFavourites(user, entity.getEntityType());
        }
    }

    public boolean isFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        notNull("user", user);
        notNull("entity", entity);

        final Collection<Long> ids = getFavouriteIds(user, entity.getEntityType());
        return ids.contains(entity.getId());
    }

    public Collection<Long> getFavouriteIds(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        notNull("user", user);
        notNull("entityType", entityType);

        return getFavouriteIds(user.getKey(), entityType);
    }

    public Collection<Long> getFavouriteIds(final String userKey, final SharedEntity.TypeDescriptor<?> entityType)
    {
        notNull("user", userKey);
        notNull("entityType", entityType);

        final Key key = new Key(userKey, entityType);
        Collection<Long> ids = favouritesCache.get(key);

        return ids;
    }

    public void removeFavouritesForUser(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        notNull("user", user);
        notNull("entityType", entityType);

        //We order it this way to ensure correctness.
        try
        {
            delegateStore.removeFavouritesForUser(user, entityType);
        }
        finally
        {
            flushFavourites(user, entityType);
        }
    }

    public void removeFavouritesForEntity(final SharedEntity entity)
    {
        try
        {
            delegateStore.removeFavouritesForEntity(entity);
        }
        finally
        {
            favouritesCache.removeAll();
        }
    }

    public void updateSequence(final ApplicationUser user, final List<? extends SharedEntity> favouriteEntities)
    {
        try
        {
            delegateStore.updateSequence(user, favouriteEntities);
        }
        finally
        {
            if (!favouriteEntities.isEmpty())
            {
                final SharedEntity entity = favouriteEntities.get(0);
                flushFavourites(user, entity.getEntityType());
            }
        }
    }

    private void flushFavourites(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> typeDescriptor)
    {
        if (user != null)
        {
            favouritesCache.remove(new Key(user.getKey(), typeDescriptor));
        }
    }

    @Override
    public void start() throws Exception
    {
    }

    /**
     * Key object for the cache.
     *
     * @since v3.13
     */
    private static class Key implements Serializable
    {
        private final String userKey;
        private final SharedEntity.TypeDescriptor<?> type;

        public Key(final String userKey, final SharedEntity.TypeDescriptor<?> type)
        {
            Assertions.notNull("user", userKey);
            Assertions.notNull("type", type);

            this.userKey = userKey;
            this.type = type;
        }

        public String getUserKey()
        {
            return userKey;
        }

        public SharedEntity.TypeDescriptor<?> getType()
        {
            return type;
        }

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

            if (!type.equals(key.type))
            {
                return false;
            }
            if (!userKey.equals(key.userKey))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = userKey.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    private class KeyCollectionCacheLoader implements CacheLoader<Key, Collection<Long>>
    {
        @Override
        public Collection<Long> load(final Key key)
        {
            String userKey = (key.getUserKey());
            SharedEntity.TypeDescriptor entityType = key.getType();
            Collection<Long> ids = delegateStore.getFavouriteIds(userKey, entityType);
            if (ids == null)
            {
                ids = Collections.emptyList();
            }
            return ids;
        }
    }
}
