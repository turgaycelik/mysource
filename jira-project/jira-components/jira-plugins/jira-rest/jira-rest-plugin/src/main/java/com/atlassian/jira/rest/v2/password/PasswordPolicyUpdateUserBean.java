package com.atlassian.jira.rest.v2.password;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents a password policy check when updating an existing user.
 *
 * @since v6.1
 */
public class PasswordPolicyUpdateUserBean
{
    @JsonProperty
    private String username;

    @JsonProperty
    private String oldPassword;

    @JsonProperty
    private String newPassword;

    public String getUsername()
    {
        return username;
    }

    public String getOldPassword()
    {
        return oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }
}
