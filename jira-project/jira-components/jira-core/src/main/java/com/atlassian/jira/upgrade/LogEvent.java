package com.atlassian.jira.upgrade;

import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * Event subclass that intercepts the {@link Event#setProgress(int)} in order to provide some status to
 * the log file (using log4j).
 */
public class LogEvent extends Event
{
    private final Logger log;
    private final String message;

    /**
     * Constructor
     *
     * @param log a logger that is used for outputting reindex progress. Must not be null
     * @throws NullPointerException if the logger is null
     */
    public LogEvent(Logger log, String type, String description, String message)
    {
        super(EventType.get(type), description);

        if (log == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires an instance of " + Logger.class.getName());
        }
        this.log = log;
        if (message == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires a progress message to display");
        }
        this.message = message;
    }

    public void setProgress(int progress)
    {
        if (getProgress() != progress)
        {
            log.info(MessageFormat.format(message, new Object[] {new Integer(progress)}));
        }
        super.setProgress(progress);
    }
}