package com.atlassian.jira.web.action.setup;

import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;

/**
 * A simple class to store in the current session the username of the user who is setting up JIRA.
 *
 * @since v5.0
 */
public class SetupAdminUserSessionStorage implements Serializable
{
    public static final String SESSION_KEY = SetupAdminUserSessionStorage.class.getCanonicalName();

    private final String username;

    public SetupAdminUserSessionStorage(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
}
