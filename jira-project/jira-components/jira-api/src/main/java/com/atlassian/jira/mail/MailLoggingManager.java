package com.atlassian.jira.mail;

import com.atlassian.mail.server.MailServer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Central place for manipulating logging/debug state for mail servers.
 * Part of JIRA API because mail handlers (e.g. {@link com.atlassian.jira.service.services.mail.MailFetcherService} and
 * its descendants) may need to access JIRA mail logging configuration in order to behave well.
 *
 * Apache Log4J (1.2+) is used as the logging mechanism and its Loggers are directly exposed via API
 *
 * @since v5.0
 */
public interface MailLoggingManager
{
    /**
     * Retrieves currently configured logging level for outgoing mail
     *
     * @return currently configured logging level for outgoing mail
     */
    Level getOutgoingMailLoggingLevel();

    /**
     * Retrieves currently configured logging level for incoming mail
     *
     * @return currently configured logging level for incoming mail
     */
    Level getIncomingMailLoggingLevel();

    /**
     * Sets new logging level for outgoing mail
     *
     * @param loggingLevel new logging level
     */
    void setOutgoingMailLoggingLevel(Level loggingLevel);

    /**
     * Sets new logging level for incoming mail
     *
     * @param loggingLevel new logging level
     */
    void setIncomingMailLoggingLevel(Level loggingLevel);

    /**
     * Clients willing to log to outgoing mail log (by default written to a separate log file)
     * can use this logger.
     *
     * @return logger which can be used to log outgoing mail handling details
     */
    Logger getOutgoingMailLogger();

    /**
     * Clients willing to log to incoming mail log (by default written to a separate log file)
     * can use this logger.
     *
     * @return logger which can be used to log incoming mail handling details
     */
    Logger getIncomingMailLogger();

    /**
     * Clients willing to log to incoming mail log (by default written to a separate log file)
     * can use this method to obtain their more specific logger, which will inherit
     * the appenders from the main incoming mail logger, thus information sent there
     * will be logged also in the incoming mail log file.
     *
     * @param subname additional name for this logger. It will be concatenated with the main incoming mail logger using "." (dot).
     * @return logger which can be used to log incoming mail handling details
     */
    Logger getIncomingMailChildLogger(String subname);

    boolean isMailRelatedLogger(Logger logger);

    /**
     * Normally you don't need to use this method, unless you explicitely creates instances of MailServer classes,
     * then it's advisable to configure their logging level by calling this method.
     *
     * @param mailServer configures logging for given server.
     */
    void configureLogging(MailServer mailServer);
}
