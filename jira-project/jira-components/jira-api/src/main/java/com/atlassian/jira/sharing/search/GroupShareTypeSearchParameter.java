package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.type.ShareType;

/**
 * Represents the search parameters when searching for Group ShareTypes. The object can be used to search for SharePermissions
 * that share with a particular group by passing a group name. The object can also be used to find all the group SharePermissions
 * that a user can see by passing a null group.
 *
 * @since v3.13
 */
public class GroupShareTypeSearchParameter extends AbstractShareTypeSearchParameter
{
    private final String groupName;

    public GroupShareTypeSearchParameter()
    {
        this(null);
    }

    public GroupShareTypeSearchParameter(final String groupName)
    {
        super(ShareType.Name.GROUP);

        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final GroupShareTypeSearchParameter that = (GroupShareTypeSearchParameter) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        return (groupName != null ? groupName.hashCode() : 0);
    }
    ///CLOVER:ON
}
