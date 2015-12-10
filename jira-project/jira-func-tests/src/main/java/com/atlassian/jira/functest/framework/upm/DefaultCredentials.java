package com.atlassian.jira.functest.framework.upm;


import org.apache.http.auth.UsernamePasswordCredentials;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 6.3
 */
public class DefaultCredentials
{

    public static UsernamePasswordCredentials getDefaultAdminCredentials()
    {
        return getUsernamePasswordCredentials("admin", "admin");
    }

    public static UsernamePasswordCredentials getUsernamePasswordCredentials(final String username, final String password)
    {
        checkNotNull(username, "username");
        checkNotNull(password, "password");
        return new UsernamePasswordCredentials(username, password);
    }
}