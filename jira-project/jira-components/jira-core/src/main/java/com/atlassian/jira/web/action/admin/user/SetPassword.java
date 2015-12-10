/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class SetPassword extends ViewUser
{
    private final PasswordPolicyManager passwordPolicyManager;
    private final UserUtil userUtil;
    private final List<WebErrorMessage> passwordErrors = new ArrayList<WebErrorMessage>();

    private String password;
    private String confirm;

    public SetPassword(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserUtil userUtil,
            final UserPropertyManager userPropertyManager, UserManager userManager,
            final PasswordPolicyManager passwordPolicyManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.userUtil = notNull("userUtil", userUtil);
        this.passwordPolicyManager = notNull("passwordPolicyManager", passwordPolicyManager);
    }

    protected void doValidation()
    {
        super.doValidation();
        if (!hasPermission(Permissions.ADMINISTER))
        {
            addErrorMessage(getText("admin.errors.must.be.admin.to.set.password"));
        }
        if (!isRemoteUserPermittedToEditSelectedUser())
        {
            addErrorMessage(getText("admin.errors.must.be.sysadmin.to.set.sysadmin.password"));
        }

        if (!TextUtils.stringSet(password))
        {
            addError("password", getText("admin.errors.must.specify.a.password"));
        }
        else if (!password.equals(confirm))
        {
            addError("confirm", getText("admin.errors.two.passwords.do.not.match"));
        }
        else
        {
            final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(getApplicationUser(), null, password);
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
        new UserPasswordActionHelper(this,userUtil).setPassword(getUser(), password);
        if (invalidInput())
        {
            return ERROR;
        }
        return returnComplete("/secure/admin/user/ViewUser.jspa?name=" + JiraUrlCodec.encode(getName()) + "&showPasswordUpdateMsg=true");
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<WebErrorMessage> getPasswordErrors()
    {
        return passwordErrors;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }
}
