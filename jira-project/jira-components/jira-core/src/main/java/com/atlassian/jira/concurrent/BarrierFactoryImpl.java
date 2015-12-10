package com.atlassian.jira.concurrent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import javax.annotation.Nonnull;

/**
 * Barrier factory. Caches created barriers indefinitely.
 *
 * @since v5.2
 */
public class BarrierFactoryImpl implements BarrierFactory
{
    private final Cache<String, Barrier> barriers = CacheBuilder.newBuilder().build(new CacheLoader<String, Barrier>()
    {
        @Override
        public Barrier load(String barrierName) throws Exception
        {
            return new BarrierImpl(barrierName);
        }
    });

    @Nonnull
    @Override
    public Barrier getBarrier(String barrierName)
    {
        return barriers.getUnchecked(barrierName);
    }
}
