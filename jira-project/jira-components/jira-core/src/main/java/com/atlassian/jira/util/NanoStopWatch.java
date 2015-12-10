package com.atlassian.jira.util;

public class NanoStopWatch
{
    private static long startTime;

    public static void start()
    {
        startTime = System.nanoTime();
    }

    public static void sysout(String message)
    {
        long nanos = System.nanoTime() - startTime;
        System.out.println(message + ' ' + nanos);
        startTime = System.nanoTime();
    }
}
