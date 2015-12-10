package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.atlassian.cache.Cache;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.Visitor;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.apache.log4j.Logger;

import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntityKey.getKeyLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntityKey.getKeyPreserveCase;

/**
 * Wrapper for the common logic of the canonical caches used for users and groups.
 *
 * @since v6.3
 */
abstract class UserOrGroupCache<T extends UserOrGroupStub>
{
    private static final Logger LOG = Logger.getLogger(UserOrGroupCache.class);
    private final AtomicBoolean forceRefresh = new AtomicBoolean();
    private final String entityName;
    private final ResettableLazyReference<Cache<DirectoryEntityKey,T>> cacheRef;

    UserOrGroupCache(String entityName)
    {
        this.entityName = entityName;

        cacheRef = new ResettableLazyReference<Cache<DirectoryEntityKey, T>>()
        {
            @Override
            protected Cache<DirectoryEntityKey,T> create() throws Exception
            {
                final Cache<DirectoryEntityKey,T> cache = createCache();
                buildCacheIfRequired(cache);
                return cache;
            }
        };
    }

    Cache<DirectoryEntityKey,T> getCache()
    {
        try
        {
            return cacheRef.get();
        }
        catch (LazyReference.InitializationException ex)
        {
            // Don't allow lazy reference initialization failure to persist indefinitely
            cacheRef.reset();
            throw ex;
        }
    }

    void refresh()
    {
        forceRefresh.set(true);
        cacheRef.reset();
        getCache();  // force immediate refresh
    }

    @Nullable
    T getCaseInsensitive(final long directoryId, final String name)
    {
        final Cache<DirectoryEntityKey,T> cache = getCache();

        // Optimization: try with the case we are given, first.
        final DirectoryEntityKey key1 = getKeyPreserveCase(directoryId, name);
        final T result = cache.get(key1);
        if (result != null)
        {
            return result;
        }

        // Optimization: skip lookup if lowercase operation returned the same string instance, which it usually will
        // if the name is already in lowercase
        //noinspection StringEquality
        final DirectoryEntityKey key2 = getKeyLowerCase(directoryId, name);
        return (key1.getName() != key2.getName()) ? cache.get(key2) : null;
    }

    @Nullable
    T getCaseSensitive(final long directoryId, final String name)
    {
        return getCache().get(getKeyPreserveCase(directoryId, name));

    }

    Collection<DirectoryEntityKey> getKeys()
    {
        return getCache().getKeys();
    }

    DirectoryEntityKey put(T value)
    {
        final DirectoryEntityKey key = DirectoryEntityKey.getKeyFor(value);
        getCache().put(key, value);
        return key;
    }

    void remove(long directoryId, final String name)
    {
        remove(DirectoryEntityKey.getKeyLowerCase(directoryId, name));
    }

    void remove(DirectoryEntityKey key)
    {
        getCache().remove(key);
    }

    void removeAll()
    {
        getCache().removeAll();
    }




    /**
     * Build our cache if it isn't already populated or if a forced refresh was requested.
     */
    void buildCacheIfRequired(final Cache<DirectoryEntityKey,T> cache)
    {
        final Lock lock = getLock();
        lock.lock();
        try
        {
            buildCacheIfRequiredUnderLock(cache);
        }
        finally
        {
            lock.unlock();
        }
    }


    @ClusterSafe("the use of Cache.getKeys() is safe, as users and groups are fully populated, canonical caches")
    @GuardedBy("getLock()")
    private void buildCacheIfRequiredUnderLock(final Cache<DirectoryEntityKey,T> cache)
    {
        if (forceRefresh.get())
        {
            buildCacheForced(cache);
            return;
        }

        final long expectedCount = countAllUsingDatabase();
        final long actualCount = cache.getKeys().size();

        // Note: Should be equal, but we'll guard against being unlucky enough to look right when a user gets added
        if (actualCount >= expectedCount)
        {
            LOG.debug("Cache size matched entity count (once under lock); skipping cache rebuild for " + entityName);
        }
        else
        {
            LOG.debug("Cache size (" + actualCount + ") < " + entityName + " database size; refreshing...");
            visitAllUsingDatabase(new PutIfAbsentVisitor(cache));
        }
    }

    @GuardedBy("getLock()")
    private void buildCacheForced(final Cache<DirectoryEntityKey,T> cache)
    {
        LOG.debug("Forced hard refresh of " + entityName + " cache");
        cache.removeAll();
        visitAllUsingDatabase(new PutVisitor(cache));
        forceRefresh.set(false);
    }

    @ClusterSafe("the use of Cache.getKeys() is safe, as users and groups are fully populated, canonical caches")
    public List<T> getAll()
    {
        final Cache<DirectoryEntityKey,T> cache = getCache();
        final Collection<DirectoryEntityKey> keys = cache.getKeys();
        final List<T> values = new ArrayList<T>(keys.size());

        for (DirectoryEntityKey key : keys)
        {
            final T value = cache.get(key);
            if (value != null)
            {
                values.add(value);
            }
        }
        return values;
    }

    public List<T> getAllInDirectory(long directoryId)
    {
        return getAllInDirectory(directoryId, Functions.<T>identity());
    }

    public List<String> getAllNamesInDirectory(long directoryId)
    {
        return getAllInDirectory(directoryId, new GetNameFunction<T>());
    }

    @ClusterSafe("the use of Cache.getKeys() is safe, as users and groups are fully populated, canonical caches")
    public void visitAllInDirectory(final long directoryId, Visitor<T> visitor) {
        final Cache<DirectoryEntityKey,T> cache = getCache();
        for (DirectoryEntityKey key : cache.getKeys())
        {
            if (key.getDirectoryId() == directoryId)
            {
                final T value = cache.get(key);
                if (value != null)
                {
                    visitor.visit(value);
                }
            }
        }
    }

    @ClusterSafe("the use of Cache.getKeys() is safe, as users and groups are fully populated, canonical caches")
    public <R> List<R> getAllInDirectory(final long directoryId, final Function<T,R> function)
    {
        final List<R> list = new ArrayList<R>(256);
        visitAllInDirectory(directoryId, new Visitor<T>()
        {
            @Override
            public void visit(final T element)
            {
                list.add(function.get(element));
            }
        });
        return list;
    }

    public boolean isCacheInitialized()
    {
        return cacheRef.isInitialized();
    }


    /**
     * Called to provide a {@code Lock} (presumably a {@code ClusterLock}) which will guard cache refreshes
     * to prevent multiple nodes from doing this work at once.
     *
     * @return the lock that guards cache initialization
     */
    abstract Lock getLock();

    /**
     * Called to construct the actual cache object.  This is done lazily on first access to the cache so that
     * the expensive user cache bootstrap will not block construction of the application container.
     *
     * @return the cache that will hold the directory entities
     */
    abstract Cache<DirectoryEntityKey,T> createCache();

    /**
     * Called to obtain a count of entities in the database.
     * Used to determine whether or not a cache refresh can be skipped because the cache entry count
     * meets expectations, which is assumed to mean that it was bootstrapped successfully be another node.
     *
     * @return the count of entries that the database indicates we should find in the cache
     */
    abstract long countAllUsingDatabase();

    /**
     * Called to populate the cache.
     * The visitor should be called exactly once for every user/group found in the database.
     * The lock returned by {@link #getLock()} is always acquired before this method is called
     * to prevent multiple cluster nodes from performing this operation at once.
     *
     * @param visitor a callback for streaming the results into the cache
     */
    @GuardedBy("getLock()")
    abstract void visitAllUsingDatabase(final Visitor<T> visitor);



    class PutVisitor implements Visitor<T>
    {
        private final Cache<DirectoryEntityKey,T> cache;

        PutVisitor(final Cache<DirectoryEntityKey,T> cache)
        {
            this.cache = cache;
        }

        @Override
        public void visit(final T value)
        {
            cache.put(DirectoryEntityKey.getKeyFor(value), value);
        }
    }

    class PutIfAbsentVisitor implements Visitor<T>
    {
        private final Cache<DirectoryEntityKey,T> cache;

        PutIfAbsentVisitor(final Cache<DirectoryEntityKey,T> cache)
        {
            this.cache = cache;
        }

        @Override
        public void visit(final T value)
        {
            final DirectoryEntityKey key = DirectoryEntityKey.getKeyFor(value);
            if (cache.get(key) == null)
            {
                cache.put(key, value);
            }
        }
    }

    static class GetNameFunction<T extends UserOrGroupStub> implements Function<T,String>
    {
        @Override
        public String get(final UserOrGroupStub value)
        {
            return value.getName();
        }
    }
}

