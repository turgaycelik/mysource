package com.atlassian.jira.util.log;

import com.atlassian.jira.logging.RollOverLogAppender;
import com.atlassian.jira.startup.FormattedLogMsg;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Enumeration;

/**
 * A helper class to mark the logs for support reasons
 *
 * @since v5.0
 */
public class LogMarker
{
    private static final Logger log = Logger.getLogger(LogMarker.class);

    /**
     * Puts a log marker in ALL the JIRA logs to help with say support or debugging
     *
     * @param message an optional message to be put at that mark
     */
    public static void markLogs(final String message)
    {
        /*
          This relies in thge log4j configuration mapping this logger to all
          the appenders out there.  if this is not true then log marking will not be in
          synch as expected

          At the time of writing for example :

         log4j.logger.com.atlassian.jira.util.log.LogMarker  = INFO, console, nowarnconsole, filelog, soapaccesslog, soapdumplog, httpaccesslog, httpdumplog, sqllog, slowquerylog, xsrflog, securitylog

         */
        final FormattedLogMsg logMsg = new FormattedLogMsg(log);
        if (message != null && !message.isEmpty())
        {
            logMsg.add(message);
        }
        logMsg.printMessage(Level.INFO, true);
    }

    /**
     * This will rollover the All the JIRA logs and then makr the new log with the specific message
     *
     * @param message an optional message to be put at that mark
     */
    public static void rolloverAndMark(final String message)
    {
        //JRA-26192: writing null to logs first to initialize any unintialized appenders
        markLogs(StringUtils.EMPTY);

        rollover(log);
        markLogs(message);
    }

    public static void rollover(final Logger log)
    {
        Logger logger = log;
        while (logger != null && !logger.getAllAppenders().hasMoreElements())
        {
            logger = (Logger) logger.getParent();
        }

        if (logger == null)
        {
            return;
        }

        for (Enumeration enumeration = logger.getAllAppenders(); enumeration.hasMoreElements(); )
        {
            final Appender appender = (Appender) enumeration.nextElement();
            if (appender instanceof RollOverLogAppender)
            {
                rolloverAsAppropropriate((RollOverLogAppender) appender);
            }
        }
    }

    private static void rolloverAsAppropropriate(RollOverLogAppender rollOverLogAppender)
    {
        File logFile = new File(rollOverLogAppender.getFile());
        if (logFile.exists() && logFile.length() > 0)
        {
            rollOverLogAppender.rollOver();
        }
    }
}
