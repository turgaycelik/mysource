package com.atlassian.jira.concurrent;

import com.atlassian.annotations.Internal;

import javax.annotation.Nonnull;

/**
 * Manager for {@link Barrier} instances.
 *
 * @since v5.2
 */
@Internal
public interface BarrierFactory
{
    /**
     * Returns the {@code Barrier} with the given name, creating one if necessary.
     *
     * @param barrierName a String containing the barrier name
     * @return a Barrier
     */
    @Nonnull
    Barrier getBarrier(String barrierName);
}
