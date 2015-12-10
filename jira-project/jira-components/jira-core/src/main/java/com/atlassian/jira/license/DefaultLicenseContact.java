package com.atlassian.jira.license;

/**
 * A simple representation of a contact person associated with a JIRA License (name and email)
 *
 * @since v6.1
 */
public class DefaultLicenseContact implements LicenseDetails.LicenseContact
{
    private final String name;
    private final String email;

    public DefaultLicenseContact(String name, String email)
    {
        this.name = name;
        this.email = email;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getEmail()
    {
        return email;
    }
}
