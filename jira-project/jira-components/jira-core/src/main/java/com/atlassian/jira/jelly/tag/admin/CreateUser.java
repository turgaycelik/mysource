/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jelly.NewUserContextAccessor;
import com.atlassian.jira.jelly.NewUserContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class CreateUser extends UserAwareActionTagSupport implements NewUserContextAccessor
{
    private static final Logger log = Logger.getLogger(CreateUser.class);
    private static final String KEY_USERNAME = "username";

    private NewUserContextAccessor newUserContextAccessor = new NewUserContextAccessorImpl(this);
    private boolean sendEmail = false;

    public CreateUser()
    {
        setActionName("AddUser");
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        if (getProperties().containsKey(KEY_USERNAME))
        {
            setNewUser(getProperty(KEY_USERNAME));
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousNewUser();
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_USERNAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.NEW_USERNAME };
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


    public boolean isSendEmail()
    {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail)
    {
        this.sendEmail = sendEmail;
    }
}
