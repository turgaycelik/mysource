package com.atlassian.jira.webtests;

/**
 * Group names for func tests.
 *
 * @since v4.2
 */
public final class Groups
{
    private Groups()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static final String USERS = "jira-users";
    public static final String DEVELOPERS = "jira-developers";
    public static final String ADMINISTRATORS = "jira-administrators";
}
