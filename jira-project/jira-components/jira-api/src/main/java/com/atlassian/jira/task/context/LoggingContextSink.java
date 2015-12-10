package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * Simple sink that writes output to a Logger.
 *
 * @since v3.13
 */
@Internal
class LoggingContextSink implements Sink
{
    private final Logger log;
    private String name = "";
    private final String msg;
    private final Level level;

    LoggingContextSink(final Logger log, final String msg, final Level level)
    {
        Assertions.notNull("logger", log);
        this.log = log;
        this.msg = msg;
        this.level = level;
    }

    LoggingContextSink(final Logger log, final String msg)
    {
        this(log, msg, Level.DEBUG);
    }

    public void setName(final String name)
    {
        Assertions.notNull("name", name);
        this.name = name;
    }

    public void updateProgress(final int progress)
    {
        log.log(level, MessageFormat.format(msg, new Object[]{new Integer(progress), name}));
    }
}
