package com.atlassian.jira.issue.fields.option;

import com.atlassian.crowd.embedded.api.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GroupOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private Group group;
    private String alternateName;
    private Set children;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public GroupOption(Group group)
    {
        this.group = group;
    }

    public GroupOption(String alternateName)
    {
        this.alternateName = alternateName;
    }

    public GroupOption(Group group, String alternateName)
    {
        this.group = group;
        this.alternateName = alternateName;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return null;
    }

    /**
     * return the name of this group. if alternateName has been set, use that instead of the name set in the group
     */
    public String getName()
    {
        return alternateName != null ? alternateName : (group != null ? group.getName() : "");
    }

    /**
     * get the group name regardless of whether an alternate name has been set
     */
    public String getRawName()
    {
        return group != null ? group.getName() : "";
    }

    public String getDescription()
    {
        return null;
    }

    public Group getGroup()
    {
        return group;
    }

    /**
     * get the users to be displayed under this groups option. Does not necessarily have the actual users of this group
     */
    public List getChildOptions()
    {
        return children != null ? new ArrayList(children) : Collections.EMPTY_LIST;
    }

    public void addChildOption(AbstractChildOption childOption)
    {
        if (childOption != null)
        {
            getChildren().add(childOption);
            childOption.setParentOption(this);
        }
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    private Set getChildren()
    {
        if (children == null)
            children = new TreeSet();

        return children;
    }
}
