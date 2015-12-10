/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.login;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jelly.ActionTagSupport;
import com.atlassian.jira.jelly.WebWorkAdaptor;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import webwork.action.ActionContext;

public class Login extends ActionTagSupport
{
    private static final transient Logger log = Logger.getLogger(Login.class);
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private boolean hasPreviousUser = false;
    private User previousUser = null;
    private boolean hasPreviousUsername = false;
    private String previousUsername = null;
    private User previousAuthContextUser = null;

    private final JiraAuthenticationContext authenticationContext;
    private final UserManager userManager;

    public Login(JiraAuthenticationContext authenticationContext, UserManager userManager)
    {
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        setActionName("TestLogin");
    }

    public void doTag(XMLOutput output) throws JellyTagException
    {
        log.debug("TestLogin.doTag");

        String username = getProperty(KEY_USERNAME);
        String password = getProperty(KEY_PASSWORD);

        ActionContext oldContext = null;

        if (!mapContainsAll(getRequiredProperties(), getProperties()))
        {
            if (username == null)
            {
                final User user = authenticationContext.getLoggedInUser();
                if (user == null)
                {
                    throw new MissingAttributeException("User not logged in you must specify: username = 'null' and password = " + password);
                }
                setContextVariables(user);
            }
            else
            {
                final User user = userManager.getUser(username);
                if (user == null)
                {
                    throw new MissingAttributeException("User not logged in you must specify: username = " + username + " and password = " + password);
                }
                else
                {
                    // Record the old user
                    previousAuthContextUser = authenticationContext.getLoggedInUser();
                    setContextVariables(user);
                }
            }
        }
        else
        {
            if ((username == null) || (username.length() == 0) || (password == null) || (password.length() == 0))
            {
                throw new MissingAttributeException("username = " + username + ", password = " + password);
            }
            else
            {
                // JRA-15276 - we want to keep the old context around so we can push it back onto the stack after
                // we are done tricking webwork for the jelly tags
                oldContext = ActionContext.getContext();
                // We set an empty context because it is modified by reference later on, don't want to mess up
                // our old context
                ActionContext.setContext(new ActionContext());

                try
                {
                    // Record the old user no matter what, we will want to log this user back in.
                    previousAuthContextUser = authenticationContext.getLoggedInUser();

                    final User user = userManager.getUser(username);
                    if (getWebWorkAdaptor().authenticateUser(this, username, password, output))
                    {
                        // Replace the user with the newly logged in user
                        authenticationContext.setLoggedInUser(user);
                        setContextVariables(user);
                    }
                }
                catch (Exception e)
                {
                    try
                    {
                        WebWorkAdaptor.writeErrorToXmlOutput(output, new StringBuffer("Login"), "User: " + username + " does not exist", this);
                    }
                    catch (SAXException e1)
                    {
                        throw new JellyTagException(e1);
                    }
                }
            }
        }

        try
        {
            // Invoke the nested tag
            Script body = getBody();
            if (body != null)
            {
                body.run(getContext(), output);
            }
        }
        finally
        {
            endTagExecution(output);
            if (oldContext != null)
            {
                // Make sure we restore the old context if there was one.
                ActionContext.setContext(oldContext);
            }
        }
    }

    private void setContextVariables(final User user)
    {
        User prevUser = (User) getContext().getVariable(JellyTagConstants.USER);
        String prevUserName = (String) getContext().getVariable(JellyTagConstants.USERNAME);
        setPreviousUser(prevUser);
        getContext().setVariable(JellyTagConstants.USER, user);
        setPreviousUsername(prevUserName);
        getContext().setVariable(JellyTagConstants.USERNAME, user.getName());
    }

    protected void endTagExecution(XMLOutput output)
    {
        authenticationContext.setLoggedInUser(previousAuthContextUser);
        if (hasPreviousUser)
            getContext().setVariable(JellyTagConstants.USER, getPreviousUser());
        if (hasPreviousUsername)
            getContext().setVariable(JellyTagConstants.USERNAME, getPreviousUsername());
    }

    public String[] getRequiredContextVariables()
    {
        return new String[0];
    }

    public String[] getRequiredProperties()
    {
        return new String[]{KEY_USERNAME, KEY_PASSWORD};
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[]{JellyTagConstants.USER, JellyTagConstants.USERNAME};
    }

    private User getPreviousUser()
    {
        return previousUser;
    }

    private void setPreviousUser(User previousUser)
    {
        this.hasPreviousUser = true;
        this.previousUser = previousUser;
    }

    private String getPreviousUsername()
    {
        return previousUsername;
    }

    private void setPreviousUsername(String previousUsername)
    {
        this.hasPreviousUsername = true;
        this.previousUsername = previousUsername;
    }
}
