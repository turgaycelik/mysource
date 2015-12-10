package com.atlassian.jira.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import com.atlassian.jira.cluster.ClusterSafe;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;

/**
 * Default implementation of {@link SearchExtractorRegistrationManager}
 *
 * @since 6.2
 */
public class DefaultSearchExtractorRegistrationManager implements SearchExtractorRegistrationManager
{
    private final Logger LOG = Logger.getLogger(DefaultSearchExtractorRegistrationManager.class);
    private final ConcurrentMap<Class<?>, ImmutableSet<EntitySearchExtractor<?>>> extractors = Maps.newConcurrentMap();
    @ClusterSafe("Only concerned with local component registration")
    private final Cache<Class<?>, Lock> locks = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Lock>()
    {
        @Override
        public Lock load(@Nonnull final Class<?> key) throws Exception
        {
            return new ReentrantLock();
        }
    });

    @Override
    public <T> Collection<EntitySearchExtractor<T>> findExtractorsForEntity(final Class<T> entityClass)
    {
        Preconditions.checkNotNull(entityClass, "entityClass parameter cannot be null");
        if (extractors.containsKey(entityClass))
        {
            //noinspection unchecked
            return (Collection) extractors.get(entityClass);
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public <T> void register(final EntitySearchExtractor<? super T> extractor, final Class<T> entityClass)
    {

        Preconditions.checkNotNull(extractor, "extractor parameter cannot be null");
        Preconditions.checkNotNull(entityClass, "entityClass parameter cannot be null");

        final Lock lock = tryLock(entityClass);
        try
        {
            final Set<EntitySearchExtractor<?>> entitySearchExtractors = extractors.get(entityClass);
            extractors.put(entityClass, entitySearchExtractors == null ?
                    ImmutableSet.<EntitySearchExtractor<?>>of(extractor) :
                    ImmutableSet.<EntitySearchExtractor<?>>builder().
                            addAll(entitySearchExtractors).
                            add(extractor).build());
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public <T> void unregister(final EntitySearchExtractor<? super T> extractor, Class<T> entityClass)
    {
        Preconditions.checkNotNull(extractor, "extractor parameter cannot be null");
        Preconditions.checkNotNull(entityClass, "entityClass parameter cannot be null");
        final Lock lock = tryLock(entityClass);
        try
        {
            final ImmutableSet<EntitySearchExtractor<?>> entitySearchExtractors = extractors.get(entityClass);

            if (entitySearchExtractors == null || !entitySearchExtractors.contains(extractor))
            {
                return;
            }
            extractors.put(entityClass, ImmutableSet.copyOf(Sets.filter(entitySearchExtractors, new Predicate<EntitySearchExtractor<?>>()
            {
                @Override
                public boolean apply(final EntitySearchExtractor<?> input)
                {
                    return input != extractor && !input.equals(extractor);
                }
            })));
        }
        finally
        {
            lock.unlock();
        }
    }

    private <T> Lock tryLock(final Class<T> entityClass)
    {
        final Lock lock = locks.getUnchecked(entityClass);
        try
        {
            if (lock.tryLock(30, TimeUnit.SECONDS))
            {
                return lock;
            }
            else
            {
                LOG.error("Could not obtain lock for DefaultSearchExtractorRegistrationManager");
                throw new LockNotAcquiredException("Could not obtain lock for DefaultSearchExtractorRegistrationManager");
            }
        }
        catch (final InterruptedException e)
        {
            LOG.error("Could not obtain lock for DefaultSearchExtractorRegistrationManager");
            throw new LockNotAcquiredException("Could not obtain lock for DefaultSearchExtractorRegistrationManager",e);
        }
    }

    private static class LockNotAcquiredException extends RuntimeException
    {
        private LockNotAcquiredException(final String message)
        {
            super(message);
        }

        private LockNotAcquiredException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }
}
