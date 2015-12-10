package it.com.atlassian.jira.plugin.headernav;

import javax.annotation.Nullable;

/**
 *
 */
public enum Users
{
    SysAdmin("admin", "admin"),
    ProjectAdmin("charlie", "charlie"),
    AuthenticatedUser("bob", "bob"),
    Anonymous(null, null);

    private final String userName;
    private final String password;

    private Users(@Nullable final String userName, @Nullable final String password)
    {
        this.userName = userName;
        this.password = password;
    }

    public boolean requiresAuthentication()
    {
        return userName != null && password != null;
    }

    @Nullable
    public String getUserName()
    {
        return userName;
    }

    @Nullable
    public String getPassword()
    {
        return password;
    }
}
