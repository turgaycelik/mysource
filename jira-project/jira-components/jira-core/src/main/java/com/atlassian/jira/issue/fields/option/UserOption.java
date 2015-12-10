package com.atlassian.jira.issue.fields.option;

import com.atlassian.crowd.embedded.api.User;

public class UserOption extends AbstractChildOption implements Option, Comparable
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private User user;
    private String alternateName;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public UserOption(User user)
    {
        this.user = user;
    }

    public UserOption(String alternateName)
    {
        this.alternateName = alternateName;
    }

    public UserOption(User user, String alternateName)
    {
        this.user = user;
        this.alternateName = alternateName;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getName()
    {
        return alternateName != null ? alternateName : (user != null ? user.getName() : "");
    }

    public User getUser()
    {
        return user;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserOption)) return false;
        if (!super.equals(o)) return false;

        final UserOption userOption = (UserOption) o;

        if (user != null ? !user.equals(userOption.user) : userOption.user != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    public int compareTo(Object obj)
    {
        if (obj == null || !(obj instanceof UserOption))
            return 1;

        User other = ((UserOption) obj).user;

        if (user != null)
        {
            if (other != null)
                return compareNames(user.getName(), other.getName());
            else
                return 1;
        }
        else
        {
            if (other != null)
                return -1;
            else
                return 0;
        }
    }

    private int compareNames(String name1, String name2)
    {
        if (name1 != null)
        {
            if (name2 != null)
                return name1.compareTo(name2);
            else
                return 1;
        }
        else
        {
            if (name2 != null)
                return -1;
            else
                return 0;
        }
    }
}
