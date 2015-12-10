package com.atlassian.jira.test;

/**
 * Standard log names for JIRA tests.
 *
 * @since 5.1
 */
public class TestLoggers
{
    private TestLoggers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static String forTestSuite(String suiteName)
    {
        return "com.atlassian.jira.test." + suiteName;
    }
}
