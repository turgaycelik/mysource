package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.cluster.logging.LoggingManager;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@WebSudoRequired
public class ConfigureLogging extends ViewLogging
{
    private String loggerName;
    private String levelName;

    private Logger logger;

    public ConfigureLogging(MailServerManager mailServerManager, MailLoggingManager mailLoggingManager,
            LoggingManager loggingManager)
    {
        super(mailServerManager, mailLoggingManager, loggingManager);
    }

    // Protected -----------------------------------------------------
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Logger logger = getLogger();
        loggingManager.setLogLevel(logger.getName(), levelName);
        return returnComplete("ViewLogging.jspa");
    }

    public Logger getLogger()
    {
        if (logger == null)
        {
            if ("root".equals(loggerName))
                logger = getRootLogger();
            else
                logger = Logger.getLogger(loggerName);
        }
        return logger;
    }

    public String getLoggerName()
    {
        return loggerName;
    }

    public void setLoggerName(String loggerName)
    {
        this.loggerName = loggerName;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }
}
