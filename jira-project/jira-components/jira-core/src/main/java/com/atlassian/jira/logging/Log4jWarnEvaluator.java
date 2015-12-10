package com.atlassian.jira.logging;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

/**
 * This is apparently the only way to set the log level for an STMP appender.
 * See log4j.properties.
 *
 * @since v4.2
 */
public class Log4jWarnEvaluator implements TriggeringEventEvaluator
{
    public boolean isTriggeringEvent(final LoggingEvent event)
    {
         return event.getLevel().isGreaterOrEqual(Level.WARN);
    }
}
