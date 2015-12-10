package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;

/**
 * Marks classes that perform time computations and are using {@link com.atlassian.core.util.Clock}s.
 *
 * @since v4.3
 */
public interface ClockAware
{

    /**
     * Clock used by this instance.
     *
     * @return clock
     */
    Clock clock();
}
