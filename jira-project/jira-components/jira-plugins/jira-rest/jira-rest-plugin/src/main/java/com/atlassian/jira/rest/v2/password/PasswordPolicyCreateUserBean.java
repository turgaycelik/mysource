package com.atlassian.jira.rest.v2.password;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents a password policy check when creating a new user.
 *
 * @since v6.1
 */
public class PasswordPolicyCreateUserBean
{
    @JsonProperty
    private String username;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String emailAddress;

    @JsonProperty
    private String password;

    public String getUsername()
    {
        return username;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getPassword()
    {
        return password;
    }
}
