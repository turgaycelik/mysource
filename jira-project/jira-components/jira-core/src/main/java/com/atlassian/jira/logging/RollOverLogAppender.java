package com.atlassian.jira.logging;

/**
 * A simple interface to describe log appenders that can be rolled over
 *
 * @since v5.0
 */
public interface RollOverLogAppender
{
    /**
     * @return the name of the current log file that can be rolled over
     */
    String getFile();

    /**
     * Cause a log rollover to happen
     */
    void rollOver();
}
