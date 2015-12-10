/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;

public class GroupContextAccessorImpl implements GroupContextAccessor, GroupAware
{
    private final Tag tag;
    private boolean hasGroup = false;
    private String group = null;

    public GroupContextAccessorImpl(Tag tag)
    {
        this.tag = tag;
    }

    public JellyContext getContext()
    {
        return tag.getContext();
    }

    public void setGroup(String groupname)
    {
        setPreviousGroup();
        resetGroupContext();
        setNewUserContext(groupname);
    }

    public void setGroup(Group group)
    {
        setPreviousGroup();
        resetGroupContext();
        setGroupContext(group);
    }

    public void loadPreviousGroup()
    {
        if (hasGroup)
        {
            resetGroupContext();
            setGroup(group);
            hasGroup = false;
            group = null;
        }
    }

    private void setPreviousGroup()
    {
        if (hasGroup())
        {
            hasGroup = true;
            group = getGroupName();
        }
    }

    private void resetGroupContext()
    {
        getContext().removeVariable(JellyTagConstants.GROUP_NAME);
    }

    private void setNewUserContext(String groupname)
    {
        final Group group = ComponentAccessor.getGroupManager().getGroup(groupname);
        setGroupContext(group);
    }

    private void setGroupContext(final Group group)
    {
        getContext().setVariable(JellyTagConstants.GROUP_NAME, group.getName());
    }

    public boolean hasGroup()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.GROUP_NAME);
    }

    public String getGroupName()
    {
        if (hasGroup())
            return (String) getContext().getVariable(JellyTagConstants.GROUP_NAME);
        else
            return null;
    }

    public Group getGroup()
    {
        return ComponentAccessor.getGroupManager().getGroup(getGroupName());
    }
}
