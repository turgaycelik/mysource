package com.atlassian.jira.issue.fields.option;

public abstract class AbstractChildOption extends AbstractOption
{
    private GroupOption parentOption;

    public String getId()
    {
        return null;
    }

    /**
     * return the name of this user. if alternateName has been set, use that instead of the name set in the group
     */

    public String getDescription()
    {
        return null;
    }

    public GroupOption getParentOption()
    {
        return parentOption;
    }

    public void setParentOption(GroupOption groupOption)
    {
        this.parentOption = groupOption;
    }

    public String getParentName()
    {
        return parentOption != null ? parentOption.getName() : "";
    }
}
