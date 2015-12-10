package com.atlassian.jira.util;

/**
 * Used to shut something down.
 *
 * @since v4.0
 */
public interface Shutdown
{
    /**
     * Shutdown. Should not throw any exceptions.
     */
    void shutdown();
}
