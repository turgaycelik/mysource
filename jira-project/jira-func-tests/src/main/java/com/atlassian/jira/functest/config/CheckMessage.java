package com.atlassian.jira.functest.config;

import org.apache.commons.lang.StringUtils;

/**
 * A class that contains a message and check id.
 */
public final class CheckMessage
{
    private final String message;
    private final String checkId;

    public CheckMessage(final String message, final String checkId)
    {
        final String s = StringUtils.trimToNull(message);
        this.message = s != null ? s : "<unknown>";
        this.checkId = StringUtils.trimToNull(checkId);
    }

    public CheckMessage(final String message)
    {
        this (message, null);
    }

    public String getMessage()
    {
        return message;
    }

    public String getFormattedMessage()
    {
        if (checkId != null)
        {
            return String.format("[%s] - %s", checkId, message);
        }
        else
        {
            return message;
        }
    }

    public String getCheckId()
    {
        return checkId;
    }

    @Override
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

        final CheckMessage that = (CheckMessage) o;

        if (checkId != null ? !checkId.equals(that.checkId) : that.checkId != null)
        {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (checkId != null ? checkId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return getFormattedMessage();
    }
}
