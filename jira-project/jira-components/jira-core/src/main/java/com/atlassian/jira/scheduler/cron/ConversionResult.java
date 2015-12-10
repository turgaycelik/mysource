package com.atlassian.jira.scheduler.cron;

/**
 * Represents a converted trigger to a cron string
*/
public class ConversionResult
{
    public String cronString;
    public boolean hasLoss;

    public ConversionResult(boolean hasLoss, String cronString)
    {
        this.hasLoss = hasLoss;
        this.cronString = cronString.trim();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ConversionResult that = (ConversionResult) o;

        if (hasLoss != that.hasLoss)
        {
            return false;
        }
        if (!cronString.equals(that.cronString))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = cronString.hashCode();
        result = 31 * result + (hasLoss ? 1 : 0);
        return result;
    }

    public String toString()
    {
        return cronString + ((hasLoss) ? " lossy" : "");
    }
}
