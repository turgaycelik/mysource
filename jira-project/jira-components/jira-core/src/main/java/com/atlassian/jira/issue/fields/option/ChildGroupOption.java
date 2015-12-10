package com.atlassian.jira.issue.fields.option;

import com.atlassian.crowd.embedded.api.Group;

public class ChildGroupOption extends AbstractChildOption implements Option, Comparable
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private Group child;
    private String alternateName;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public ChildGroupOption(Group child)
    {
        this.child = child;
    }

    public ChildGroupOption(String alternateName)
    {
        this.alternateName = alternateName;
    }

    public ChildGroupOption(Group child, String alternateName)
    {
        this.child = child;
        this.alternateName = alternateName;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    /**
     * return the name of this child. if alternateName has been set, use that instead of the name set in the group
     */
    public String getName()
    {
        return alternateName != null ? alternateName : (child != null ? child.getName() : "");
    }

    public Group getChild()
    {
        return child;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ChildGroupOption)) return false;
        if (!super.equals(o)) return false;

        final ChildGroupOption childOption = (ChildGroupOption) o;

        if (child != null ? !child.equals(childOption.child) : childOption.child != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (child != null ? child.hashCode() : 0);
        return result;
    }

    public int compareTo(Object obj)
    {
        if (obj == null || !(obj instanceof ChildGroupOption))
            return 1;

        Group other = ((ChildGroupOption) obj).child;

        if (child != null)
        {
            if (other != null)
                return compareNames(child.getName(), other.getName());
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
