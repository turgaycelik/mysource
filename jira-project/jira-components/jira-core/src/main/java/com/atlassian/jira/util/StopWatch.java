package com.atlassian.jira.util;

/**
 * @since v5.0.6
 */
public class StopWatch
{
    private long startTime;
    private long lastIntervalTime;

    public StopWatch()
    {
        this.startTime = System.currentTimeMillis();
        this.lastIntervalTime = startTime;
    }

    public long getIntervalTime()
    {
        long now = System.currentTimeMillis();
        long intervalTime = now - lastIntervalTime;
        lastIntervalTime = now;
        return intervalTime;
    }

    public long getTotalTime()
    {
        return System.currentTimeMillis() - startTime;
    }

    public void sysout(String message)
    {
        System.out.println(message + ' ' + getIntervalTime());
    }
}
