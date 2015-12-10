/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.jelly.GroupContextAccessor;
import com.atlassian.jira.jelly.GroupContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class CreateGroup extends UserAwareActionTagSupport implements GroupContextAccessor
{
    private static final Logger log = Logger.getLogger(CreateGroup.class);
    private static final String KEY_GROUPNAME = "addName";
    private GroupContextAccessor groupContextAccessor = new GroupContextAccessorImpl(this);

    public CreateGroup()
    {
        setActionName("GroupBrowser!add");
        ignoreErrors = true;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        final String GROUP_NAME = "group-name";
        if (getProperties().containsKey(GROUP_NAME))
            setProperty(KEY_GROUPNAME, getProperty(GROUP_NAME));
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        if (getProperties().containsKey(KEY_GROUPNAME))
            setGroup(getProperty(KEY_GROUPNAME));
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousGroup();
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_GROUPNAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.GROUP_NAME };
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
