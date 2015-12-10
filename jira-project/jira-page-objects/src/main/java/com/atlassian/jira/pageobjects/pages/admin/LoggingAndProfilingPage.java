package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.log4j.Level;

import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class LoggingAndProfilingPage extends AbstractJiraAdminPage
{

    public enum LoggingStatus {
        OFF, ON, DEBUG
    }

    @Override
    public String linkId()
    {
        return null;
    }

    @Override
    public TimedCondition isAt()
    {
        return null;
    }

    @Override
    public String getUrl()
    {
        return "secure/admin/workflows/ViewLogging.jspa";
    }

    public LoggingStatus getOutgoingMailLoggingStatus() {
        return null;
    }

    public LoggingStatus getIncomingMailLoggingStatus() {
        return null;
    }

    public void setOutgoingMailLoggingStatus(LoggingStatus loggingStatus)
    {

    }

    public void setIncomingMailLoggingStatus(LoggingStatus loggingStatus)
    {

    }

    public void addCustomLogger(String logger, Level level)
    {

    }

    @Nullable
    public Level getLevel(String loger)
    {
        return null;
    }

}
