package com.atlassian.jira.functest.config.xml;

/**
 * Common XML check IDs
 *
 * @since v6.0
 */
public final class Checks
{

    private Checks()
    {
        throw new AssertionError("Don't instantiate me");
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Don't clone me");
    }

    public static final String UPGRADE = "upgrade";
    public static final String MAIL_SERVER = "mailserver";
}
