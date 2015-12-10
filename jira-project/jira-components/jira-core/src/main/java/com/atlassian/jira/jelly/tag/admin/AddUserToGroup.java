/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.GroupAware;
import com.atlassian.jira.jelly.GroupContextAccessor;
import com.atlassian.jira.jelly.GroupContextAccessorImpl;
import com.atlassian.jira.jelly.NewUserAware;
import com.atlassian.jira.jelly.NewUserContextAccessor;
import com.atlassian.jira.jelly.NewUserContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

public class AddUserToGroup extends UserAwareActionTagSupport implements NewUserAware, GroupAware, NewUserContextAccessor, GroupContextAccessor
{
    private static final String KEY_USERNAME = "name";
    private static final String KEY_GROUPS_TO_JOIN = "groupsToJoin";
    private static final String KEY_JOIN = "join";
    private NewUserContextAccessor newUserContextAccessor = new NewUserContextAccessorImpl(this);
    private GroupContextAccessor groupContextAccessor = new GroupContextAccessorImpl(this);

    public AddUserToGroup()
    {
        setActionName("EditUserGroups");
    }

    protected void preContextValidation()
    {
        final String NEW_USERNAME = "username";
        if (getProperties().containsKey(NEW_USERNAME))
        {
            setNewUser(getProperty(NEW_USERNAME));
        }

        final String GROUP_NAME = "group-name";
        if (getProperties().containsKey(GROUP_NAME))
        {
            setGroup(getProperty(GROUP_NAME));
        }
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (hasNewUsername())
            setProperty(KEY_USERNAME, getNewUsername());
        if (hasGroup())
            setProperty(KEY_GROUPS_TO_JOIN, getGroupName());
        setProperty(KEY_JOIN, "true");
    }

    public String[] getRequiredContextVariables()
    {
        final String[] requiredContextVariables = super.getRequiredContextVariables();
        final String[] required = new String[requiredContextVariables.length + 2];
        System.arraycopy(requiredContextVariables, 0, required, 0, requiredContextVariables.length);
        required[required.length - 2] = JellyTagConstants.NEW_USERNAME;
        required[required.length - 1] = JellyTagConstants.GROUP_NAME;
        return required;
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousNewUser();
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_USERNAME, KEY_GROUPS_TO_JOIN };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
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

    public boolean hasNewUsername()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.NEW_USERNAME);
    }

    public String getNewUsername()
    {
        return (String) getContext().getVariable(JellyTagConstants.NEW_USERNAME);
    }

    public void setNewUser(String username)
    {
        newUserContextAccessor.setNewUser(username);
    }

    public void setNewUser(User user)
    {
        newUserContextAccessor.setNewUser(user);
    }

    public void loadPreviousNewUser()
    {
        newUserContextAccessor.loadPreviousNewUser();
    }

    public void setGroup(String groupname)
    {
        groupContextAccessor.setGroup(groupname);
    }

    public void setGroup(Group group)
    {
        groupContextAccessor.setGroup(group);
    }

    public void loadPreviousGroup()
    {
        groupContextAccessor.loadPreviousGroup();
    }
}
