package com.atlassian.jira.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheException;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import net.jcip.annotations.GuardedBy;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.emptyList;

/**
 * Caching implementation of {@link com.atlassian.jira.user.UserHistoryStore}.
 * This is very broken, but no more broken than any of the other caches.
 *
 * @since v4.0
 */
@EventComponent
public class CachingUserHistoryStore implements UserHistoryStore, Startable
{
    private static final int DEFAULT_MAX_THRESHOLD = 10;
    static final int DEFAULT_MAX_ITEMS = 20;
    private static final Logger log = Logger.getLogger(CachingUserHistoryStore.class);

    private final Cache<Key, List<UserHistoryItem>> cache;

    /**
     * Lock on the user name.
     */
    @ClusterSafe("A given user should only be using one node at a time, even if logged into multiple")
    private final Function<ApplicationUser, ManagedLock> lockManager = ManagedLocks.weakManagedLockFactory(new Function<ApplicationUser, String>()
    {
        public String get(final ApplicationUser input)
        {
            return input.getKey();
        }
    });

    private final OfBizUserHistoryStore delegatingStore;
    private final ApplicationProperties applicationProperties;
    private final int maxThreshold;

    public CachingUserHistoryStore(@Nonnull final OfBizUserHistoryStore delegatingStore, @Nonnull final ApplicationProperties applicationProperties, @Nonnull final CacheManager cacheManager)
    {
        this(delegatingStore, applicationProperties, cacheManager, DEFAULT_MAX_THRESHOLD);
    }

    CachingUserHistoryStore(@Nonnull final OfBizUserHistoryStore delegatingStore, @Nonnull final ApplicationProperties applicationProperties,
            final CacheManager cacheManager, int maxThreshold)
    {
        this.delegatingStore = notNull("delegatingStore", delegatingStore);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.maxThreshold = maxThreshold;

        this.cache = cacheManager.getCache(CachingUserHistoryStore.class.getName() + ".cache",
                new DelegatingStoreCacheLoader(),
                new CacheSettingsBuilder().local().expireAfterAccess(15, TimeUnit.MINUTES).build());
    }

    @Override
    public void start() throws Exception
    {

    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        cache.removeAll();
    }

    public void addHistoryItem(final ApplicationUser user, @Nonnull final UserHistoryItem historyItem)
    {
        notNull("user", user);
        notNull("historyItem", historyItem);

        final AddHistoryResult result = lockManager.get(user).withLock(new Supplier<AddHistoryResult>()
        {
            public AddHistoryResult get()
            {
                return addCachedHistoryItem(user, historyItem);
            }
        });

        // JRADEV-15768
        // Don't call the delegatingStore while the lock is held!  Was causing deadlock between DB and JIRA.
        try
        {
            if (result.create)
            {
                try
                {
                    delegatingStore.addHistoryItemNoChecks(user, historyItem);
                    if (result.toDelete != null)
                    {
                        delegatingStore.expireOldHistoryItems(user, historyItem.getType(), result.toDelete);
                    }
                }
                catch (DataAccessException e)
                {
                    //JRADEV-15907, JRA-30247: Remove the current entry if its there in-case the cache was out of sync
                    //with the database and try the add again.
                    if (delegatingStore.removeHistoryItem(user, historyItem))
                    {
                        delegatingStore.addHistoryItemNoChecks(user, historyItem);
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
            else
            {
                delegatingStore.updateHistoryItemNoChecks(user, historyItem);
            }
        }
        catch (DataAccessException e)
        {
            //JRADEV-15907, JRA-30247: We don't want a database error here to cause other operations to fail. If the user
            //loses a bit of history then so be it. We just clear to cache to make sure we don't get into a bad state.
            flushCache(historyItem.getType(), user);

            // JRADEV-20710  If you suspect a problem, please change your logging settings rather than increasing
            // the logging level for this exception.  Due to JRADEV-15768, it can happen due to simple race
            // conditions from concurrent threads modifying the history at once.  This is unavoidable without
            // reintroducing the initial deadlock, so the SQL contraint violations need to be tolerated without
            // spamming production logs.
            log.debug("Unable to add user history to store. Ignoring error.", e);
        }
    }

    @GuardedBy("lockManager.get(user)")
    AddHistoryResult addCachedHistoryItem(@Nonnull final ApplicationUser user, @Nonnull final UserHistoryItem historyItem)
    {
        final UserHistoryItem.Type type = historyItem.getType();
        final List<UserHistoryItem> history = cache.get(new Key(user.getKey(), type));

        // If we find it in the list, then we can do the update in place and the list didn't grow
        if (removeCachedHistoryItem(history, historyItem))
        {
            history.add(0, historyItem);
            return AddHistoryResult.SIMPLE_UPDATE;
        }

        // new item
        history.add(0, historyItem);

        // don't prune every time, wait until it gets MAX_THRESHOLD more than max
        final int maxItems = getMaxItems(historyItem.getType(), applicationProperties);
        if (history.size() <= maxItems + maxThreshold)
        {
            return AddHistoryResult.SIMPLE_CREATE;
        }

        // only keep first maxItems issues.
        final List<String> entitiesToDelete = new ArrayList<String>();
        while (history.size() > maxItems)
        {
            final UserHistoryItem item = history.remove(maxItems);
            entitiesToDelete.add(item.getEntityId());
        }
        return new AddHistoryResult(entitiesToDelete);
    }

    @GuardedBy("lockManager.get(user)")
    private boolean removeCachedHistoryItem(
            @Nonnull final Iterable<UserHistoryItem> history, @Nonnull final UserHistoryItem historyItem)
    {
        for (final Iterator<UserHistoryItem> iter = history.iterator(); iter.hasNext();)
        {
            final UserHistoryItem currentHistoryItem = iter.next();
            if (currentHistoryItem.getEntityId().equals(historyItem.getEntityId()))
            {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public List<UserHistoryItem> getHistory(@Nonnull final UserHistoryItem.Type type, @Nonnull final String userKey)
    {
        notNull("userKey", userKey);
        notNull("type", type);

        try
        {
            return ImmutableList.copyOf(cache.get(new Key(userKey, type)));
        }
        catch (CacheException e)
        {
            if (e.getCause() instanceof DataAccessException)
            {
                //JRADEV-15907, JRA-30247: We don't want a database error here to cause other operations to fail. If the user
                //loses a bit of history then so be it. Log an move on.

                log.debug("Unable to get user history items. Returning empty list.", e);
                return emptyList();
            }
            else
            {
                throw e;
            }
        }
    }

    @Nonnull
    public List<UserHistoryItem> getHistory(@Nonnull final UserHistoryItem.Type type, @Nonnull final ApplicationUser user)
    {
        notNull("user", user);
        return getHistory(type, user.getKey());
    }

    public Set<UserHistoryItem.Type> removeHistoryForUser(@Nonnull final ApplicationUser user)
    {
        notNull("user", user);
        final Set<UserHistoryItem.Type> typesRemoved = delegatingStore.removeHistoryForUser(user);
        for (final UserHistoryItem.Type type : typesRemoved)
        {
            flushCache(type, user);
        }
        return typesRemoved;
    }

    public void removeHistoryOlderThan(@Nonnull final Long timestamp)
    {
        notNull("timestamp", timestamp);
        delegatingStore.removeHistoryOlderThan(timestamp);
        cache.removeAll();
    }

    private void flushCache(final UserHistoryItem.Type type, final ApplicationUser user)
    {
        cache.remove(new Key(user.getKey(), type));
    }

    public static int getMaxItems(final UserHistoryItem.Type type, final ApplicationProperties applicationProperties)
    {
        final String maxItemsForTypeStr = applicationProperties.getDefaultBackedString("jira.max." + type.getName() + ".history.items");
        final int maxItems = DEFAULT_MAX_ITEMS;
        try
        {
            if (StringUtils.isNotBlank(maxItemsForTypeStr))
            {
                return Integer.parseInt(maxItemsForTypeStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max." + type.getName() + ".history.items'.  Should be a number.");
        }

        final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
        try
        {
            if (StringUtils.isNotBlank(maxItemsStr))
            {
                return Integer.parseInt(maxItemsStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }
        return maxItems;
    }

    /**
     * Get from the underlying store if not already cached.
     */
    final class DelegatingStoreCacheLoader implements CacheLoader<Key, List<UserHistoryItem>>
    {
        public List<UserHistoryItem> load(final Key key)
        {
            final List<UserHistoryItem> history = delegatingStore.getHistory(key.type, key.userKey);
            // Delegating store's history isn't ours to toy with!
            return new ArrayList<UserHistoryItem>(history);
        }
    }

    /**
     * Key object for the cache.
     *
     * @since v4.0
     */
    @VisibleForTesting
    static final class Key implements Serializable
    {
        private final String userKey;
        private final UserHistoryItem.Type type;

        public Key(final String userKey, final UserHistoryItem.Type type)
        {
            notNull("user", userKey);
            notNull("type", type);

            this.userKey = userKey;
            this.type = type;
        }

        public String getUserKey()
        {
            return userKey;
        }

        public UserHistoryItem.Type getType()
        {
            return type;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (!(other instanceof Key))
            {
                return false;
            }

            final Key otherKey = (Key) other;
            return type.equals(otherKey.getType()) && getUserKey().equals(otherKey.getUserKey());
        }

        @Override
        public int hashCode()
        {
            int result;
            result = getUserKey().hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    /**
     * State object recording what happened during a call to
     * {@link #addCachedHistoryItem(ApplicationUser, UserHistoryItem)}
     * so we'll know what to keep the delegating store in synch.
     */
    static class AddHistoryResult
    {
        static final AddHistoryResult SIMPLE_CREATE = new AddHistoryResult(true, null);
        static final AddHistoryResult SIMPLE_UPDATE = new AddHistoryResult(false, null);

        final boolean create;
        final List<String> toDelete;

        AddHistoryResult(List<String> toDelete)
        {
            this(true, toDelete);
        }

        private AddHistoryResult(boolean create, List<String> toDelete)
        {
            this.create = create;
            this.toDelete = toDelete;
        }
    }
}
