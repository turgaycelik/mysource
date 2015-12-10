package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.NullComparator;

/**
 * A value entered into a user searcher (e.g. current user, a specific user).
 *
 * @since 5.2
 */
public class UserSearchInput implements
        Comparable<UserSearchInput>
{
    public static enum InputType
    {
        CURRENT_USER, EMPTY, GROUP, USER
    }

    private Object object;
    private final InputType type;
    private final String value;

    private UserSearchInput(InputType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    /**
     * @return An instance representing the "currentUser()" value.
     */
    public static UserSearchInput currentUser()
    {
        return new UserSearchInput(InputType.CURRENT_USER, null);
    }

    /**
     * @return An instance representing the "empty" value (e.g. unassigned).
     */
    public static UserSearchInput empty()
    {
        return new UserSearchInput(InputType.EMPTY, null);
    }

    /**
     * @param name The name of the group.
     * @return An instance representing a particular group.
     */
    public static UserSearchInput group(String name)
    {
        return new UserSearchInput(InputType.GROUP, name);
    }

    /**
     * @param name The user's username.
     * @return An instance representing a particular user.
     */
    public static UserSearchInput user(String name)
    {
        return new UserSearchInput(InputType.USER, name);
    }

    @Override
    public int compareTo(UserSearchInput other)
    {
        // Groups and users should be shown together.
        boolean bothGroupsOrUsers =
                (type == InputType.GROUP || type == InputType.USER) &&
                (other.type == InputType.GROUP || other.type == InputType.USER);

        if (!bothGroupsOrUsers)
        {
            return type.compareTo(other.type);
        }

        return new NullComparator().compare(getCompareValue(),
                other.getCompareValue());
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }

        if (that == null || getClass() != that.getClass())
        {
            return false;
        }

        UserSearchInput input = (UserSearchInput)that;

        if (type != input.getType())
        {
            return false;
        }

        String inputValue = input.getValue();
        if (value != null ? !value.equals(inputValue) : inputValue != null)
        {
            return false;
        }

        return true;
    }

    /**
     * @return The "comparison" value to be used in {@code compareTo}.
     */
    private String getCompareValue()
    {
        if (object != null)
        {
            if (isGroup())
            {
                return ((Group)object).getName().toLowerCase();
            }
            else if (isUser())
            {
                return ((User)object).getDisplayName().toLowerCase();
            }
        }

        return null;
    }

    /**
     * @return The actual object that corresponds to the user's input (i.e. the
     *     group/user object with the given name/username).
     */
    public Object getObject()
    {
        return object;
    }

    /**
     * @return The type of the input value (i.e. current user, empty, etc.).
     */
    public InputType getType()
    {
        return type;
    }

    /**
     * @return The input value (i.e. the name of the group/user).
     */
    public String getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    /**
     * @return Whether the instance represents the "currentUser()" value.
     */
    public boolean isCurrentUser()
    {
        return type == InputType.CURRENT_USER;
    }

    /**
     * @return Whether the instance represents the "empty" value.
     */
    public boolean isEmpty()
    {
        return type == InputType.EMPTY;
    }

    /**
     * @return Whether the instance represents a particular group.
     */
    public boolean isGroup()
    {
        return type == InputType.GROUP;
    }

    /**
     * @return Whether the instance represents a particular user.
     */
    public boolean isUser()
    {
        return type == InputType.USER;
    }

    /**
     * @param object The object to set.
     */
    public void setObject(Object object)
    {
        this.object = object;
    }
}
