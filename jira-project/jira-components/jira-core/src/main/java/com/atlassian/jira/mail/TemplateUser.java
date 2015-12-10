package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.lang.StringUtils;

/**
 * Provide a simple delegation around a User. This class catches exceptions and returns simple strings that are
 * suitable for use within templates. See JRA-15551 for the motivation.
 *
 * @see TemplateIssue
 * @since v4.0
 */
public class TemplateUser
{
    final private User user;

    public static TemplateUser getUser(final User user)
    {
        return new TemplateUser(user);
    }

    /**
     * Returns the email address of the user
     * @return the email address of the user
     */
    public String getEmailAddress()
    {
        String email;
        try
        {
            email = user.getEmailAddress();
            if (email == null)
            {
                email = "";
            }
        }
        catch (Exception exception)
        {
            email = "";
        }
        return email;
    }

    /**
     * Returns the email address of the user
     * @return the email address of the user
     * @deprecated Please use {@link #getEmailAddress()}. Since v4.3
     */
    public String getEmail()
    {
        return getEmailAddress();
    }

    /**
     * @return display name (eg. full name) of the user.
     */
    public String getDisplayName()
    {
        String fullName;
        try
        {
            if (user == null)
            {
                fullName = getName();
            }
            else
            {
                fullName = user.getDisplayName();
                if (StringUtils.isBlank(fullName))
                {
                    fullName = getName();
                }
            }
        }
        catch (Exception exception)
        {
            fullName = getName();
        }
        return fullName;
    }

    /**
     * @return full name of the user.
     * @deprecated please use {@link #getDisplayName()}. Since v4.3
     */
    public String getFullName()
    {
        return getDisplayName();
    }

    public String getName()
    {
        String name;
        try
        {
            if (user != null)
            {
                name = user.getName();
            }
            else
            {
                name = "Anonymous";
            }
        }
        catch (Exception exception)
        {
            name = "";
        }
        return name;
    }

    public int hashCode() {return user.hashCode();}

    public String toString()
    {
        return getName();
    }

    public boolean equals(final Object o) {return user.equals(o);}

    private TemplateUser(final User user)
    {
        this.user = user;
    }

}
