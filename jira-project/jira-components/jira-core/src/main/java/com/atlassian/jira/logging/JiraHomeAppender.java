package com.atlassian.jira.logging;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.startup.JiraHomeStartupCheck;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A log4j appender that will log output data to the JIRA.HOME/log directory. It accepts the same options as the {@link
 * org.apache.log4j.RollingFileAppender}.
 * <p/>
 * The appender may be called before JIRA.HOME is ready to be used. In this situation the appender will buffer the log
 * events until the JIRA.HOME is ready. Once ready, all the events will be flushed in order.
 * <p/>
 * The appender will revert to its old behaviour (creating a file in the working directory) if JIRA.HOME is not configured
 * correctly.
 * <p/>
 *
 * @since v4.1
 */
public final class JiraHomeAppender implements Appender, OptionHandler, RollOverLogAppender
{
    private static final int QUEUE_MAX = 100;

    private final RollingFileAppender fileAppender = new RollingFileAppender();
    private final JiraHomeStartupCheck startupCheck;
    @GuardedBy("this")
    private volatile Queue<LoggingEvent> events = new LinkedList<LoggingEvent>();
    private volatile String fileName;
    private volatile State state = State.WAITING;

    public JiraHomeAppender(final JiraHomeStartupCheck startupCheck)
    {
        this.startupCheck = startupCheck;
    }

    public JiraHomeAppender()
    {
        this(JiraHomeStartupCheck.getInstance());
    }

    private static enum State
    {
        /**
         * Waiting for JIRA.HOME to be configured. All log events will be buffered.
         */
        WAITING,

        /**
         * JIRA.HOME has been configured and log events will be written to log file.
         */
        READY,

        /**
         * JIRA.HOME appender is off either because of configuration or config error.
         */
        OFF
    }

    public int getMaxBackupIndex()
    {
        return fileAppender.getMaxBackupIndex();
    }

    public long getMaximumFileSize()
    {
        return fileAppender.getMaximumFileSize();
    }

    public void setMaxBackupIndex(final int maxBackups)
    {
        fileAppender.setMaxBackupIndex(maxBackups);
    }

    public void setMaximumFileSize(final long maxFileSize)
    {
        fileAppender.setMaximumFileSize(maxFileSize);
    }

    public void setMaxFileSize(final String value)
    {
        fileAppender.setMaxFileSize(value);
    }

    public void setFile(final String file)
    {
        this.fileName = notNull("file", file).trim();
    }

    public boolean getAppend()
    {
        return fileAppender.getAppend();
    }

    public String getFile()
    {
        return fileAppender.getFile();
    }

    public void activateOptions()
    {
        //Do nothing. We are delaying the set of the file until we can actually find the home.
    }

    public boolean getBufferedIO()
    {
        return fileAppender.getBufferedIO();
    }

    public int getBufferSize()
    {
        return fileAppender.getBufferSize();
    }

    public void setAppend(final boolean flag)
    {
        fileAppender.setAppend(flag);
    }

    public void setBufferedIO(final boolean bufferedIO)
    {
        fileAppender.setBufferedIO(bufferedIO);
    }

    public void setBufferSize(final int bufferSize)
    {
        fileAppender.setBufferSize(bufferSize);
    }

    public void setImmediateFlush(final boolean value)
    {
        fileAppender.setImmediateFlush(value);
    }

    public boolean getImmediateFlush()
    {
        return fileAppender.getImmediateFlush();
    }

    public void close()
    {
        fileAppender.close();
    }

    public String getEncoding()
    {
        return fileAppender.getEncoding();
    }

    public void setEncoding(final String value)
    {
        fileAppender.setEncoding(value);
    }

    public void setErrorHandler(final ErrorHandler eh)
    {
        fileAppender.setErrorHandler(eh);
    }

    public boolean requiresLayout()
    {
        return fileAppender.requiresLayout();
    }

    public void addFilter(final Filter newFilter)
    {
        fileAppender.addFilter(newFilter);
    }

    public void clearFilters()
    {
        fileAppender.clearFilters();
    }

    public ErrorHandler getErrorHandler()
    {
        return fileAppender.getErrorHandler();
    }

    public Filter getFilter()
    {
        return fileAppender.getFilter();
    }

    public Filter getFirstFilter()
    {
        return fileAppender.getFirstFilter();
    }

    public Layout getLayout()
    {
        return fileAppender.getLayout();
    }

    public String getName()
    {
        return fileAppender.getName();
    }

    public Priority getThreshold()
    {
        return fileAppender.getThreshold();
    }

    public void setLayout(final Layout layout)
    {
        fileAppender.setLayout(layout);
    }

    public void setName(final String name)
    {
        fileAppender.setName(name);
    }

    public void setThreshold(final Priority threshold)
    {
        fileAppender.setThreshold(threshold);
    }

    public void doAppend(final LoggingEvent event)
    {
        //Good old double check locking.
        if (state == State.READY)
        {
            fileAppender.doAppend(event);
        }
        else if (state != State.OFF)
        {
            doAppendSync(event);
        }
    }

    public void rollOver()
    {
        fileAppender.rollOver();
    }

    @ClusterSafe ("Local. This is just appending to the log file.")
    private synchronized void doAppendSync(final LoggingEvent event)
    {
        if (state == State.READY)
        {
            fileAppender.doAppend(event);
        }
        else if (state == State.WAITING)
        {
            //If we are are currently waiting for JIRA.HOME to be set, then try and configure the adapter.
            State configState = configureAppender();
            if (configState == State.WAITING)
            {
                //Still waiting for JIRA.HOME so lets queue the event.

                if (events.size() < QUEUE_MAX)
                {
                    events.add(event);
                }
                else if (events.size() == QUEUE_MAX)
                {
                    //No room left in the queue. Log a message saying that events have been dropped.
                    events.add(createDropEvent());
                }
            }
            else
            {
                if (configState == State.READY)
                {
                    //we are now ready, so log the events that have been queued.
                    for (LoggingEvent queuedEvent : events)
                    {
                        fileAppender.doAppend(queuedEvent);
                    }

                    fileAppender.doAppend(event);
                }

                //Clear out these as we wont use them again and they can be collected.
                events = null;
                fileName = null;
            }
            state = configState;
        }
        //State.OFF ignored.
    }

    private State configureAppender()
    {
        if (fileAppender.getThreshold() == Level.OFF)
        {
            return State.OFF;
        }
        else if (!startupCheck.isInitialised())
        {
            //The log directory startup check is not done yet, so just leave it un-configured.
            return State.WAITING;
        }

        if (StringUtils.isBlank(fileName))
        {
            LogLog.error("Unable to log to JIRA home: No fileName specified.");
            return State.OFF;
        }

        if (!startupCheck.isOk())
        {
            //If no JIRA home was set, then just do what we would do in 4.0 to ensure that logging happens.
            LogLog.debug("Unable to log to JIRA home: Unable to find JIRA home. Logging to working directory.");
            fileAppender.setFile(fileName);
        }
        else
        {
            File homeDirectory = startupCheck.getJiraHomeDirectory();
            final File homeLogFile = getHomeLogFile(homeDirectory, fileName);
            if (homeLogFile != null)
            {
                fileAppender.setFile(homeLogFile.getAbsolutePath());              
            }
            else
            {
                fileAppender.setFile(fileName);
            }
        }
        fileAppender.activateOptions();
        return State.READY;
    }

    static File getHomeLogFile(File home, String fileName)
    {
        //A couple of quick sanity checks for the log directory. It should be a directory.
        final File logDirectory = normalizeFile(new File(home, JiraHome.LOG));
        if (!logDirectory.exists())
        {
            if (!logDirectory.mkdirs())
            {
                LogLog.error("Unable to log to JIRA home: Unable to create directory '" + logDirectory + "'. Logging to working directory.");
                return null;
            }
        }
        else if (!logDirectory.isDirectory())
        {
            LogLog.error("Unable to log to JIRA home: Log directory '" + logDirectory + "' is not a directory. Logging to working directory.");
            return null;
        }

        return normalizeFile(new File(logDirectory, fileName));
    }

    private static File normalizeFile(File file)
    {
        return file.getAbsoluteFile();
    }

    private LoggingEvent createDropEvent()
    {
        return new LoggingEvent(Category.class.getName(), Logger.getLogger(getClass()), Level.ERROR, "Some log messages dropped during startup. Check application server logs.", null);
    }
}
