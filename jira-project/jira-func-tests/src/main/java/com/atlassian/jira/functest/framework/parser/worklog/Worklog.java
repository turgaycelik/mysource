package com.atlassian.jira.functest.framework.parser.worklog;

/**
 * Simple class to hold the worklog as shown on the view issue page.
 *
 * @since v3.13
 */
public class Worklog
{
    private String log;
    private String timeWorked;
    private String details;

    public Worklog()
    {
    }

    public Worklog(final String log, final String timeWorked, final String details)
    {
        this.log = log;
        this.timeWorked = timeWorked;
        this.details = details;
    }

    public String getLog()
    {
        return log;
    }

    public void setLog(final String log)
    {
        this.log = log;
    }

    public String getTimeWorked()
    {
        return timeWorked;
    }

    public void setTimeWorked(final String timeWorked)
    {
        this.timeWorked = timeWorked;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(final String details)
    {
        this.details = details;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Worklog worklog = (Worklog) o;

        if (details != null ? !details.equals(worklog.details) : worklog.details != null)
        {
            return false;
        }
        if (log != null ? !log.equals(worklog.log) : worklog.log != null)
        {
            return false;
        }
        if (timeWorked != null ? !timeWorked.equals(worklog.timeWorked) : worklog.timeWorked != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (log != null ? log.hashCode() : 0);
        result = 31 * result + (timeWorked != null ? timeWorked.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }
}
