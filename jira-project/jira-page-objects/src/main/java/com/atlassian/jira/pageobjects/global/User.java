package com.atlassian.jira.pageobjects.global;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a user from JIRA.
 *
 * @since v4.4
 */
public final class User
{
    public static final User ANONYMOUS = new User(null, null);

    public static User forUsername(String username)
    {
        return new User(username, username);
    }

    private final String userName;
    private final String fullName;
    private final String email;
    private final String password;

    public User(String userName, String fullName)
    {
        this.userName = userName;
        this.fullName = fullName;
        this.email = null;
        this.password = null;
    }

    public User(String userName, String fullName, String email, String password)
    {
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        User user = (User) o;

        if (fullName != null ? !fullName.equals(user.fullName) : user.fullName != null) { return false; }
        if (userName != null ? !userName.equals(user.userName) : user.userName != null) { return false; }
        if (email != null ? !email.equals(user.email) : user.email != null) { return false; }
        if (password != null ? !password.equals(user.password) : user.password != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
