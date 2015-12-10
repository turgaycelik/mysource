package com.atlassian.jira.task;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.log4j.Logger;

/**
 * This TaskProgressSink will output progress to the Logger if either the percentage progress changes or its been more than
 * n milliseconds since the last log entry.
 *
 * @since v3.13
 */
public class TimeBasedLogSink extends StatefulTaskProgressSink
{
    private long lastEventSeen = 0;
    private final Logger log;
    private final String description;
    private final long maxTimeBetweenEvents;

    public TimeBasedLogSink(final Logger log, final String description, final long maxTimeBetweenEvents, final TaskProgressSink delegateSink)
    {
        super(0, 100, delegateSink);

        Assertions.notNull("log", log);
        Assertions.notNull("description", description);
        Assertions.not("maxTimeBetweenEvents < 0", maxTimeBetweenEvents < 0);

        this.log = log;
        this.maxTimeBetweenEvents = maxTimeBetweenEvents;
        this.description = description;
    }

    public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
    {
        boolean logMsg = false;
        final long now = System.currentTimeMillis();
        if (getProgress() != taskProgress)
        {
            logMsg = true;
        }
        else
        {
            if (now - lastEventSeen >= maxTimeBetweenEvents)
            {
                logMsg = true;
            }
        }
        if (logMsg)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(description);
            sb.append(" [").append(taskProgress).append("%] ");
            if (currentSubTask != null)
            {
                sb.append(currentSubTask);
                sb.append(" ");
            }
            if (message != null)
            {
                sb.append(message);
            }
            log.info(sb.toString());
        }
        lastEventSeen = now;
        super.makeProgress(taskProgress, currentSubTask, message);
    }
}
