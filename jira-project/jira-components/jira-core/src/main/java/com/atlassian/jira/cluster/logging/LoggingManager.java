package com.atlassian.jira.cluster.logging;

import javax.annotation.Nonnull;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Manager to replicate logging changes across the cluster
 *
 * @since v6.3.5
 */
public interface LoggingManager
{
    /**
     * Set the logging level for a logger.
     * @param loggerName Logger name
     * @param  levelName Logging level name
     */
    void setLogLevel(@Nonnull String loggerName, @Nonnull String levelName);

    /**
     * Set the logging level for a logger.
     * @param logger Logger
     * @param  level Logging Level
     */
    void setLogLevel(@Nonnull Logger logger, @Nonnull Level level);

    /**
     * Write a marker to the log and optionally roll the logs.
     * @param msg Message to write into the logs
     * @param rollOver set to true to roll the logs
     */
    void markLogs(String msg, boolean rollOver);

    /**
     * Turn on profiling.
     */
    void enableProfiling();

    /**
     * Turn off profiling.
     */
    void disableProfiling();
}
