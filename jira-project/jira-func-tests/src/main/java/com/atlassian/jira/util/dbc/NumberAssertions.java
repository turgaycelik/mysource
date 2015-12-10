package com.atlassian.jira.util.dbc;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Utility for number assertions
 *
 * @since v4.3
 */
public final class NumberAssertions
{
    private NumberAssertions()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static int equal(String name, int actual, int expected)
    {
        if (actual != expected)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be equal to <",expected,">"));
        }
        return actual;
    }

    public static int greaterThan(String name, int actual, int floor)
    {
        if (actual <= floor)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be greater than <",floor,">"));
        }
        return actual;
    }

    public static int lessThan(String name, int actual, int ceiling)
    {
        if (actual >= ceiling)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be less than <",ceiling,">"));
        }
        return actual;
    }

    public static int greaterThanOrEqual(String name, int actual, int floor)
    {
        if (actual < floor)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be greater than or equal <",floor,">"));
        }
        return actual;
    }

    public static int lessThanOrEqual(String name, int actual, int ceiling)
    {
        if (actual > ceiling)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be less than or equal <",ceiling,">"));
        }
        return actual;
    }


    public static long equal(String name, long actual, long expected)
    {
        if (actual != expected)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be equal to <",expected,">"));
        }
        return actual;
    }

    public static long greaterThan(String name, long actual, long floor)
    {
        if (actual <= floor)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be greater than <",floor,">"));
        }
        return actual;
    }

    public static long lessThan(String name, long actual, long ceiling)
    {
        if (actual >= ceiling)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be less than <",ceiling,">"));
        }
        return actual;
    }

    public static long greaterThanOrEqual(String name, long actual, long floor)
    {
        if (actual < floor)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be greater than or equal <",floor,">"));
        }
        return actual;
    }

    public static long lessThanOrEqual(String name, long actual, long ceiling)
    {
        if (actual > ceiling)
        {
            throw new IllegalArgumentException(asString(name," is: <",actual, ">, must be less than or equal <",ceiling,">"));
        }
        return actual;
    }
}
