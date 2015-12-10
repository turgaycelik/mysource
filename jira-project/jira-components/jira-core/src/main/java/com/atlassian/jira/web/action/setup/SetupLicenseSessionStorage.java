package com.atlassian.jira.web.action.setup;

import java.io.Serializable;

/**
 * A simple class to store in the current session the firstname, lastname and email of the user who is setting up JIRA.
 *
 * @since v5.2.7
 */
public class SetupLicenseSessionStorage implements Serializable
{
    public static final String SESSION_KEY = SetupLicenseSessionStorage.class.getCanonicalName();

    private final String firstName;
    private final String lastName;
    private final String email;

    public SetupLicenseSessionStorage(String firstName, String lastName, String email)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public String getFirstName()
    {
        return firstName;
    }
}
