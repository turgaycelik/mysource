package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.cluster.logging.LoggingManager;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.logging.JiraHomeAppender;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.util.json.JsonUtil;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.jira.util.log.LogMarker;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The view action for the JIRA Profiling and Logging Admin section
 */
@WebSudoRequired
public class ViewLogging extends JiraWebActionSupport
{
    protected final LoggingManager loggingManager;
    private List<Logger> loggers;
    private Logger rootLogger;

    private String markMessage;
    private boolean rollOver;

    private static final Collection<Level> availableLevels = ImmutableList.of(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.OFF);
    private static final String VIEW_LOGGING_JSPA = "ViewLogging.jspa";
    private static final String HASH_HTTP = "#http";
    private static final String HASH_SOAP = "#soap";
    private static final String HASH_SQL = "#sql";
    private static final String HASH_PROFILING = "#profiling";
    private static final String HASH_MAIL = "#mail";

    private static final String SOAP_ACCESS_LOG = "com.atlassian.jira.soap.axis.JiraAxisSoapLog";
    private static final String SOAP_DUMP_LOG = "com.atlassian.jira.soap.axis.JiraAxisSoapLogDump";
    private static final String HTTP_ACCESS_LOG = "com.atlassian.jira.web.filters.accesslog.AccessLogFilter";
    private static final String HTTP_DUMP_LOG = "com.atlassian.jira.web.filters.accesslog.AccessLogFilterDump";
    private static final String HTTP_ACCESS_LOG_INCLUDE_IMAGES = "com.atlassian.jira.web.filters.accesslog.AccessLogFilterIncludeImages";

    private static final String SQL_LOG = "com.atlassian.jira.ofbiz.LoggingSQLInterceptor";

    private static final Set<String> LOGGER_NAME_EXEMPTION_SET;

    private final MailServerManager mailServerManager;
    private final MailLoggingManager mailLoggingManager;

    public ViewLogging(MailServerManager mailServerManager, MailLoggingManager mailLoggingManager, LoggingManager loggingManager)
    {
        this.mailServerManager = mailServerManager;
        this.mailLoggingManager = mailLoggingManager;
        this.loggingManager = loggingManager;
    }

    static
    {
        Set<String> set = new HashSet<String>();
        set.add(SQL_LOG);
        set.add(SOAP_ACCESS_LOG);
        set.add(SOAP_DUMP_LOG);
        set.add(HTTP_ACCESS_LOG);
        set.add(HTTP_DUMP_LOG);
        set.add(HTTP_ACCESS_LOG_INCLUDE_IMAGES);
        LOGGER_NAME_EXEMPTION_SET = Collections.unmodifiableSet(set);
    }

    public Collection getLoggers()
    {
        if (loggers == null)
        {
            loggers = new ArrayList<Logger>();
            final Enumeration currentLoggers = LogManager.getCurrentLoggers();
            while (currentLoggers.hasMoreElements())
            {
                Logger logger = (Logger) currentLoggers.nextElement();

                //only display categories that have an explicit level
                if (logger.getLevel() != null)
                {
                    if (!LOGGER_NAME_EXEMPTION_SET.contains(logger.getName()) && !mailLoggingManager.isMailRelatedLogger(logger))
                    {
                        loggers.add(logger);
                    }
                }
            }

            Collections.sort(loggers, new LoggerComparator());
        }
        return loggers;
    }


    public Logger getRootLogger()
    {
        if (rootLogger == null)
        {
            rootLogger = Logger.getRootLogger();
        }
        return rootLogger;
    }

    public Collection<Level> getAvailableLevels()
    {
        return availableLevels;
    }

    public String getAvailableLevelsAsJson()
    {
        return JsonUtil.toJsonString(Collections2.transform(getAvailableLevels(), new Function<Level, String>()
        {
            @Override
            public String apply(Level from)
            {
                return from.toString();
            }
        }));
    }

   //---------Log mark related methods ------------------------//

     public String doMarkLogs() throws Exception
    {
        String msg = getMarkMessage();
        loggingManager.markLogs(msg, rollOver);
        return getRedirect(VIEW_LOGGING_JSPA);
    }


    //---------Profiling related methods ------------------------//

    public String doEnableProfiling() throws Exception
    {
        loggingManager.enableProfiling();
        return getRedirect(VIEW_LOGGING_JSPA + HASH_PROFILING);
    }

    public String doDisableProfiling() throws Exception
    {
        loggingManager.disableProfiling();
        return getRedirect(VIEW_LOGGING_JSPA + HASH_PROFILING);
    }

    public boolean isProfilingEnabled()
    {
        return UtilTimerStack.isActive();
    }

    //---------SOAP related methods ------------------------//

    public String doEnableSoapAccessLog()
    {
        loggingManager.setLogLevel(getSoapAccessLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doDisableSoapAccessLog()
    {
        // always disable these as a pair.  The first is a prerequisite
        // to the second
        loggingManager.setLogLevel(getSoapAccessLogger(),Level.OFF);
        loggingManager.setLogLevel(getSoapDumpLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doEnableSoapDumpLog()
    {
        loggingManager.setLogLevel(getSoapDumpLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doDisableSoapDumpLog()
    {
        loggingManager.setLogLevel(getSoapDumpLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    private Logger getSoapAccessLogger()
    {
        return Logger.getLogger(SOAP_ACCESS_LOG);
    }

    private Logger getSoapDumpLogger()
    {
        return Logger.getLogger(SOAP_DUMP_LOG);
    }

    public boolean isSoapAccessLogEnabled()
    {
        return getSoapAccessLogger().getLevel() != Level.OFF;
    }

    public boolean isSoapDumpLogEnabled()
    {
        return getSoapDumpLogger().getLevel() != Level.OFF;
    }

    //---------HTTP related methods ------------------------//

    public String doEnableHttpAccessLog()
    {
        loggingManager.setLogLevel(getHttpAccessLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpAccessLog()
    {
        // always do these in pairs
        loggingManager.setLogLevel(getHttpAccessLogger(), Level.OFF);
        loggingManager.setLogLevel(getHttpAccessIncludeImagesLogger(), Level.OFF);
        loggingManager.setLogLevel(getHttpDumpLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpAccessLogger()
    {
        return Logger.getLogger(HTTP_ACCESS_LOG);
    }

    public boolean isHttpAccessLogEnabled()
    {
        return getHttpAccessLogger().getLevel() != Level.OFF;
    }

    public String doEnableHttpDumpLog()
    {
        loggingManager.setLogLevel(getHttpDumpLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpDumpLog()
    {
        loggingManager.setLogLevel(getHttpDumpLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpDumpLogger()
    {
        return Logger.getLogger(HTTP_DUMP_LOG);
    }

    public boolean isHttpDumpLogEnabled()
    {
        return getHttpDumpLogger().getLevel() != Level.OFF;
    }


    public String doEnableHttpAccessLogIncludeImages()
    {
        loggingManager.setLogLevel(getHttpAccessIncludeImagesLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpAccessLogIncludeImages()
    {
        loggingManager.setLogLevel(getHttpAccessIncludeImagesLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpAccessIncludeImagesLogger()
    {
        return Logger.getLogger(HTTP_ACCESS_LOG_INCLUDE_IMAGES);
    }

    public boolean isHttpAccessLogIncludeImagesEnabled()
    {
        return getHttpAccessIncludeImagesLogger().getLevel() != Level.OFF;
    }

    //---------SQL related methods ------------------------//

    public String doEnableSqlLog()
    {
        loggingManager.setLogLevel(getSqlLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doDisableSqlLog()
    {
        loggingManager.setLogLevel(getSqlLogger(), Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doEnableSqlDumpLog()
    {
        loggingManager.setLogLevel(getSqlLogger(), Level.DEBUG);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doDisableSqlDumpLog()
    {
        loggingManager.setLogLevel(getSqlLogger(), Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    private Logger getSqlLogger()
    {
        return Logger.getLogger(SQL_LOG);
    }

    public boolean isSqlLogEnabled()
    {
        return getSqlLogger().getLevel() != Level.OFF;
    }

    public boolean isSqlDumpLogEnabled()
    {
        return getSqlLogger().getLevel() == Level.DEBUG;
    }


    public boolean isAtLevel(final Logger logger, final String targetLevel)
    {
        final String loggerLevelName = logger.getEffectiveLevel().toString();
        return targetLevel.equals(loggerLevelName);
    }

    //------------- Mail logging related methods ---------------------------/

    public Logger getOutgoingMailLogger() {
        return Logger.getLogger("com.atlassian.mail");
    }


    public boolean isOutgoingMailLoggingEnabled()
    {
        return mailLoggingManager.getOutgoingMailLoggingLevel() != Level.OFF;
    }

    public boolean isOutgoingMailDebugEnabled()
    {
        return mailLoggingManager.getOutgoingMailLoggingLevel() == Level.DEBUG;
    }

    public boolean isIncomingMailLoggingEnabled()
    {
        return mailLoggingManager.getIncomingMailLoggingLevel() != Level.OFF;
    }

    public boolean isIncomingMailDebugEnabled()
    {
        return mailLoggingManager.getIncomingMailLoggingLevel() == Level.DEBUG;
    }

    public boolean isOutgoingMailServerDefined()
    {
        return mailServerManager.getDefaultSMTPMailServer() != null;
    }

    public boolean isIncomingMailServerDefined()
    {
        return !mailServerManager.getPopMailServers().isEmpty();
    }

    @Nullable
    public String getOutgoingMailFirstLogFileName() {
        return StringUtils.defaultString(getFirstFileAppenderFileName(mailLoggingManager.getOutgoingMailLogger()),
                "atlassian-jira-outgoing-mail.log");
    }

    @Nullable
    public String getIncomingMailFirstLogFileName() {
        return StringUtils.defaultString(getFirstFileAppenderFileName(mailLoggingManager.getIncomingMailLogger()),
                "atlassian-jira-incoming-mail.log");
    }


    @Nullable
    public String getFirstFileAppenderFileName(final Logger logger) {
        @SuppressWarnings("unchecked")
        final ArrayList<Appender> appenders = Collections.list(logger.getAllAppenders());
        final Appender fileAppender = Iterables.get(Iterables.filter(appenders, new Predicate<Appender>()
        {
            @Override
            public boolean apply(Appender input)
            {
                if (input instanceof JiraHomeAppender)
                {
                    return ((JiraHomeAppender) input).getFile() != null;

                }
                return Log4jKit.getLogFileName(input.getName()) != null;
            }
        }), 0, null);

        if (fileAppender == null) {
            return null;
        }


        if (fileAppender instanceof JiraHomeAppender)
        {
            return FilenameUtils.getName(((JiraHomeAppender) fileAppender).getFile());

        }

        final File file = Log4jKit.getLogFileName(fileAppender.getName());
        return file != null ? file.getName() : null;
    }


    public String doEnableOutgoingMailLogging() {
        return setOutgoingMailLoggingLevelAndRedirectBack(Level.INFO);
    }

    public String doDisableOutgoingMailLogging()
    {
        return setOutgoingMailLoggingLevelAndRedirectBack(Level.OFF);
    }

    public String doDisableOutgoingMailDebugging()
    {
        return setOutgoingMailLoggingLevelAndRedirectBack(Level.INFO);
    }

    public String doEnableOutgoingMailDebugging()
    {
        return setOutgoingMailLoggingLevelAndRedirectBack(Level.DEBUG);
    }


    
    public String doEnableIncomingMailLogging() {
        return setIncomingMailLoggingLevelAndRedirectBack(Level.INFO);
    }

    public String doDisableIncomingMailLogging()
    {
        return setIncomingMailLoggingLevelAndRedirectBack(Level.OFF);
    }

    public String doDisableIncomingMailDebugging()
    {
        return setIncomingMailLoggingLevelAndRedirectBack(Level.INFO);
    }

    public String doEnableIncomingMailDebugging()
    {
        return setIncomingMailLoggingLevelAndRedirectBack(Level.DEBUG);
    }
    
    
    private String setOutgoingMailLoggingLevelAndRedirectBack(Level loggingLevel)
    {
        mailLoggingManager.setOutgoingMailLoggingLevel(loggingLevel);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_MAIL);
    }

    private String setIncomingMailLoggingLevelAndRedirectBack(Level loggingLevel)
    {
        mailLoggingManager.setIncomingMailLoggingLevel(loggingLevel);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_MAIL);
    }


    public String getMarkMessage()
    {
        return markMessage;
    }

    public void setMarkMessage(String markMessage)
    {
        this.markMessage = markMessage;
    }

    public boolean isRollOver()
    {
        return rollOver;
    }

    public void setRollOver(boolean rollOver)
    {
        this.rollOver = rollOver;
    }

    private static class LoggerComparator implements Comparator<Logger>
    {
        public int compare(final Logger o1, final Logger o2)
        {
            if (o1 == null || o2 == null)
            {
                return 0; //lazy
            }

            String name1 = o1.getName();
            String name2 = o2.getName();

            if (name1 == null || name2 == null)
            {
                return 0; //lazy
            }
            else
            {
                return name1.compareTo(name2);
            }
        }
    }

}
