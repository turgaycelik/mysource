package com.atlassian.jira.task;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * A task progress sink that will log progress updates to the logger passed in.
 *
 * @since v4.4
 */
public class LoggingProgressSink implements TaskProgressSink
{
    private final Logger log;
    private final String message;
    private final int increment;
    private long lastProgress = 0;

    public LoggingProgressSink(final Logger logger, final String message, int increment)
    {
        this.log = logger;
        this.message = message;
        this.increment = increment;
    }

    @Override
    public void makeProgress(long taskProgress, String currentSubTask, String msg)
    {
        if ((taskProgress - lastProgress) >= increment)
        {
            log.info(MessageFormat.format(message, taskProgress));
            lastProgress = taskProgress;
        }
    }
}
