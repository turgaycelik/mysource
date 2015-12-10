/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;

public class NewUserContextAccessorImpl implements NewUserContextAccessor, NewUserAware
{
    private final Tag tag;
    private boolean hasNewUsername = false;
    private String newUsername = null;

    public NewUserContextAccessorImpl(Tag tag)
    {
        this.tag = tag;
    }

    public JellyContext getContext()
    {
        return tag.getContext();
    }

    public void setNewUser(String username)
    {
        setPreviousNewUsername();
        resetNewUserContext();
        setNewUserContext(username);
    }

    public void setNewUser(User user)
    {
        setPreviousNewUsername();
        resetNewUserContext();
        setNewUserContext(user);
    }

    public void loadPreviousNewUser()
    {
        if (hasNewUsername)
        {
            resetNewUserContext();
            setNewUser(newUsername);
            hasNewUsername = false;
            newUsername = null;
        }
    }

    private void setPreviousNewUsername()
    {
        if (hasNewUsername())
        {
            hasNewUsername = true;
            newUsername = getNewUsername();
        }
    }

    private void resetNewUserContext()
    {
        getContext().removeVariable(JellyTagConstants.NEW_USERNAME);
    }

    private void setNewUserContext(String username)
    {
        final User user = ManagerFactory.getUserManager().getUser(username);
        setNewUserContext(user);
    }

    private void setNewUserContext(final User user)
    {
        getContext().setVariable(JellyTagConstants.NEW_USERNAME, user.getName());
    }

    public boolean hasNewUsername()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.NEW_USERNAME);
    }

    public String getNewUsername()
    {
        if (hasNewUsername())
            return (String) getContext().getVariable(JellyTagConstants.NEW_USERNAME);
        else
            return null;
    }
}
