package com.atlassian.jira.mail;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.cluster.logging.LoggingManager;
import com.atlassian.jira.web.action.admin.mail.LogPrintStream;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerConfigurationHandler;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggerFactory;

import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultMailLoggingManager implements MailLoggingManager
{

    private static final Logger outgoingMailLogger = Logger.getLogger("com.atlassian.mail.outgoing");
    private static final Logger incomingMailLogger = Logger.getLogger("com.atlassian.mail.incoming");
    private final LoggingManager loggingManager;

    public DefaultMailLoggingManager(MailServerManager mailServerManager, LoggingManager loggingManager)
    {
        this.loggingManager = loggingManager;
        mailServerManager.setMailServerConfigurationHandler(new MailServerConfigurationHandler()
        {
            @Override
            public void configureMailServer(MailServer mailServer)
            {
                configureLogging(mailServer);
            }
        });
    }

    @Override
    public void configureLogging(final MailServer mailServer)
    {
        if (mailServer instanceof SMTPMailServer)
        {
            outgoingMailLogger.trace("Configuring SMTPMailServer [" + mailServer + "]");
            final SMTPMailServer smtpMailServer = (SMTPMailServer) mailServer;
            smtpMailServer.setDebugStream(new PasswordFilteringLogPrintStream(mailServer.getPassword(), outgoingMailLogger));
            mailServer.setDebug(getOutgoingMailLoggingLevel() == Level.DEBUG);
            mailServer.setLogger(outgoingMailLogger);
        }
        else if (mailServer instanceof PopMailServer)
        {
            incomingMailLogger.trace("Configuring PopMailServer [" + mailServer + "]");
            mailServer.setDebug(getIncomingMailLoggingLevel() == Level.DEBUG);
            mailServer.setDebugStream(new PasswordFilteringLogPrintStream(mailServer.getPassword(), incomingMailLogger));
            mailServer.setLogger(incomingMailLogger);
        }
        MDC.put("jira.mailserver", mailServer.getName());
    }

    @Override
    public Level getOutgoingMailLoggingLevel()
    {
        return outgoingMailLogger.getEffectiveLevel();
    }

    @Override
    public Level getIncomingMailLoggingLevel()
    {
        return incomingMailLogger.getEffectiveLevel();
    }


    @Override
    public void setOutgoingMailLoggingLevel(Level loggingLevel)
    {
        notNull("LoggingLevel cannot be null", loggingLevel);
        loggingManager.setLogLevel(outgoingMailLogger, loggingLevel);
    }

    @Override
    public void setIncomingMailLoggingLevel(Level loggingLevel)
    {
        notNull("LoggingLevel cannot be null", loggingLevel);
        loggingManager.setLogLevel(incomingMailLogger, loggingLevel);
    }

    @Override
    public Logger getOutgoingMailLogger()
    {
        return outgoingMailLogger;
    }

    @Override
    public Logger getIncomingMailLogger()
    {
        return incomingMailLogger;
    }

    @Override
    public Logger getIncomingMailChildLogger(String subname)
    {
        final String loggerName = incomingMailLogger.getName() + "." + subname;
        return LogManager.getLogger(loggerName, new LoggerFactory()
        {
            @Override
            public Logger makeNewLoggerInstance(String name)
            {
                return new Logger(name)
                {
                    @Override
                    public void setLevel(Level level)
                    {
                        // no op
                    }
                };
            }
        });
    }

    @Override
    public boolean isMailRelatedLogger(Logger logger)
    {
        final String name = logger.getName();
        return name != null && name.startsWith("com.atlassian.mail");
    }

    static class PasswordFilteringLogPrintStream extends LogPrintStream
    {
        private final String password;
        private final Pattern replacePattern;

        public PasswordFilteringLogPrintStream(String password, final Logger logger)
        {
            super(logger, Level.DEBUG);
            this.password = password;

            if (password != null)
            {
                // these two patters I found in IMAP and POP
                replacePattern = Pattern.compile("(.*PASS.*?[ \"]|.*LOGIN.*?[ \"])" + Pattern.quote(password));
            }
            else
            {

                replacePattern = null;
            }
        }

        @Override
        public String processLine(String s)
        {
            if (s != null && password != null && password.length() > 2)
            {
                s = replacePattern.matcher(s).replaceAll("$1<hidden password>");
            }
            return s;
        }

    }
}
