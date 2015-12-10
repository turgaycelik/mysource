package com.atlassian.jira.util.lang;

/**
 * An abstract builder interface.
 *
 * @param <T> type fo the target constructed object
 * @since v4.3
 */
public interface Builder<T>
{
    /**
     * Construct the target object instance.
     *
     * @return target object instance
     */
    T build();
}
