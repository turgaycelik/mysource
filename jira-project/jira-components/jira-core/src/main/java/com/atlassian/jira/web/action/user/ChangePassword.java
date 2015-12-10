/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.user.UserPasswordActionHelper;

import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ChangePassword extends JiraWebActionSupport
{
    private String current;
    private String password;
    private String confirm;
    private String username;

    private final UserUtil userUtil;
    private final UserManager userManager;
    private final CrowdService crowdService;
    private final PasswordPolicyManager passwordPolicyManager;
    private final List<WebErrorMessage> passwordErrors = new ArrayList<WebErrorMessage>();

    public ChangePassword(final UserUtil userUtil, final UserManager userManager, CrowdService crowdService,
            final PasswordPolicyManager passwordPolicyManager)
    {
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.crowdService = crowdService;
        this.passwordPolicyManager = passwordPolicyManager;
    }

    public String doDefault() throws Exception
    {
        final ApplicationUser current = getLoggedInApplicationUser();

        if (current == null || !current.getUsername().equals(username))
        {
            return ERROR;
        }
        if (!userManager.userCanUpdateOwnDetails(current))
        {
            addErrorMessage(getText("editprofile.not.allowed"));
            return ERROR;
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        final ApplicationUser user = notNull("user", getLoggedInApplicationUser());
        if (user == null)
        {
            addErrorMessage(getText("changepassword.could.not.find.user"));
            return;
        }
        if (!userManager.userCanUpdateOwnDetails(user))
        {
            addErrorMessage(getText("editprofile.not.allowed"));
            return;
        }

        try
        {
            crowdService.authenticate(user.getUsername(), current);
        }
        catch (FailedAuthenticationException e)
        {
            addError("current", getText("changepassword.current.password.incorrect"));
        }
        catch (Exception e)
        {
            addErrorMessage(getText("changepassword.could.not.find.user"));
        }

        if (!TextUtils.stringSet(password))
        {
            addError("password", getText("changepassword.new.password.required"));
        }
        else if (!password.equals(confirm))
        {
            addError("confirm", getText("changepassword.new.password.confirmation.does.not.match"));
        }
        else
        {
            final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(user, current, password);
            if (!messages.isEmpty())
            {
                addError("password", getText("changepassword.new.password.rejected"));
                for (WebErrorMessage message : messages)
                {
                    passwordErrors.add(message);
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final User currentUSer = getLoggedInUser();
        if (currentUSer == null || !currentUSer.getName().equals(username))
        {
            return ERROR;
        }

        new UserPasswordActionHelper(this, userUtil).setPassword(currentUSer, password);
        if (invalidInput())
        {
            return ERROR;
        }
        return returnComplete();
    }

    public boolean canUpdateUserPassword()
    {
        return userManager.canUpdateUserPassword(getLoggedInUser());
    }

    public String doSuccess()
    {
        return SUCCESS;
    }

    public void setCurrent(String current)
    {
        this.current = current;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<WebErrorMessage> getPasswordErrors()
    {
        return passwordErrors;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
