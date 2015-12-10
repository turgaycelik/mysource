/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.web.HttpServletVariables;

import com.google.common.base.Strings;

import org.apache.commons.lang.StringUtils;

import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

public class SetupAdminAccount extends AbstractSetupAction
{
    String username;
    String fullname;
    String firstname;
    String lastname;
    String email;
    String password;
    String confirm;
    String webSudoToken;
    private String EXISTING_ADMINS = "existingadmins";
    private final UserService userService;
    private final GroupManager groupManager;
    private final UserUtil userUtil;
    private final SetupSharedVariables sharedVariables;
    private final SetupProductBundleHelper productBundleHelper;
    private final InternalWebSudoManager webSudoManager;
    private final JiraLicenseService jiraLicenseService;
    private final JiraWebResourceManager webResourceManager;

    private UserService.CreateUserValidationResult result;

    private static final String JSON = "json";

    public SetupAdminAccount(UserService userService, GroupManager groupManager, UserUtil userUtil, FileFactory fileFactory, final HttpServletVariables servletVariables, final InternalWebSudoManager webSudoManager, final JiraLicenseService jiraLicenseService, final JiraWebResourceManager webResourceManager)
    {
        super(fileFactory);
        this.userService = userService;
        this.groupManager = groupManager;
        this.userUtil = userUtil;
        this.webSudoManager = webSudoManager;
        this.jiraLicenseService = jiraLicenseService;
        this.webResourceManager = webResourceManager;

        sharedVariables = new SetupSharedVariables(servletVariables, getApplicationProperties());
        productBundleHelper = new SetupProductBundleHelper(sharedVariables);
    }

    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (userUtil.getJiraAdministrators().size() > 0)
        {
            return EXISTING_ADMINS;
        }

        prePopulateFields();
        putSENIntoMetadata();

        return super.doDefault();
    }

    private void prePopulateFields()
    {
        final SetupLicenseSessionStorage sessionStorage = (SetupLicenseSessionStorage) request.getSession().getAttribute(SetupLicenseSessionStorage.SESSION_KEY);
        request.getSession().removeAttribute(SetupLicenseSessionStorage.SESSION_KEY);
        if (sessionStorage != null)
        {
            firstname = Strings.nullToEmpty(sessionStorage.getFirstName());
            lastname = Strings.nullToEmpty(sessionStorage.getLastName());
            email = Strings.nullToEmpty(sessionStorage.getEmail());

            // prepopulate fullname
            if (Strings.isNullOrEmpty(fullname))
            {
                fullname = (firstname + " " + lastname).trim();
            }

            // prepopulate username
            if (!Strings.isNullOrEmpty(firstname) && !Strings.isNullOrEmpty(lastname)) {
                username = (firstname.charAt(0) + lastname).replaceAll("\\s","").toLowerCase();
            }
        }
    }

    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if (setupAlready())
        {
            return;
        }

        result = userService.validateCreateUserForSetup(
                getLoggedInUser(), username != null ? username.trim() : username, getPassword(), getConfirm(), getEmail(), getFullname());
        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
    }

    public String doEnableWebSudo()
    {
        boolean valid = true;

        if (setupAlready())
        {
            valid = false;
        }

        if (StringUtils.isBlank(webSudoToken))
        {
            valid = false;
        }
        else if (!webSudoToken.equals(sharedVariables.getWebSudoToken()))
        {
            valid = false;
        }

        if (!valid)
        {
            ActionContext.getContext().getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        else
        {
            webSudoManager.startSession(ServletActionContext.getRequest(), ServletActionContext.getResponse());
        }

        return JSON;
    }

    protected String doExecute()
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        User administrator = null;
        try
        {
            if (!userUtil.getJiraAdministrators().isEmpty())
            {
                return EXISTING_ADMINS;
            }

            try
            {
                administrator = userService.createUserNoNotification(result);
            }
            catch (PermissionException e)
            {
                addErrorMessage(getText("signup.error.group.database.immutable", result.getUsername()));
            }
            final Group groupAdmins = getOrCreateGroup(DEFAULT_GROUP_ADMINS);
            final Group groupDevelopers = getOrCreateGroup(DEFAULT_GROUP_DEVELOPERS);
            final Group groupUsers = getOrCreateGroup(DEFAULT_GROUP_USERS);

            if ((administrator != null) && (groupAdmins != null) && (groupDevelopers != null) && (groupUsers != null))
            {
                try
                {
                    if (!groupManager.isUserInGroup(administrator.getName(), groupAdmins.getName()))
                    {
                        groupManager.addUserToGroup(administrator, groupAdmins);
                    }
                    if (!groupManager.isUserInGroup(administrator.getName(), groupDevelopers.getName()))
                    {
                        groupManager.addUserToGroup(administrator, groupDevelopers);
                    }
                    if (!groupManager.isUserInGroup(administrator.getName(), groupUsers.getName()))
                    {
                        groupManager.addUserToGroup(administrator, groupUsers);
                    }
                }
                catch (GroupNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                catch (UserNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                catch (OperationNotPermittedException e)
                {
                    throw new RuntimeException(e);
                }
                catch (OperationFailedException e)
                {
                    throw new RuntimeException(e);
                }

                // Make sure to enable admin users to change licenses during install (in case the license used is too
                // old for the JIRA version)
                final GlobalPermissionManager globalPermissionManager = ComponentAccessor.getGlobalPermissionManager();
                if (!globalPermissionManager.getGroupNames(Permissions.ADMINISTER).contains(DEFAULT_GROUP_ADMINS))
                {
                    globalPermissionManager.addPermission(Permissions.ADMINISTER, DEFAULT_GROUP_ADMINS);
                }
            }

        }
        catch (CreateException e)
        {
            throw new RuntimeException(e);
        }

        // Store the username in the session so that we can automatically log the user in, in SetupComplete
        request.getSession().setAttribute(SetupAdminUserSessionStorage.SESSION_KEY, new SetupAdminUserSessionStorage(result.getUsername()));

        if (!productBundleHelper.isNoBundleSelected())
        {
            productBundleHelper.authenticateUser(result.getUsername(), result.getPassword());
            productBundleHelper.enableWebSudo();
        }

        putSENIntoMetadata();

        return getResult();
    }

    /**
     * try and get or create a group, if we get a problem let the user know
     *
     * @param groupName the name of the group to get or create
     * @return a Group if one is found or can be created, null otherwise
     */
    private Group getOrCreateGroup(String groupName)
    {
        Group group = groupManager.getGroup(groupName);
        if (group != null)
        {
            return group;
        }
        try
        {
            return groupManager.createGroup(groupName);
        }
        catch (OperationNotPermittedException e)
        {
            addErrorMessage(getText("signup.error.group.database.immutable", groupName));
        }
        catch (InvalidGroupException e)
        {
            addErrorMessage(getText("signup.error.group.database.immutable", groupName));
        }
        return null;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getWebSudoToken()
    {
        return webSudoToken;
    }

    public void setWebSudoToken(String webSudoToken)
    {
        this.webSudoToken = webSudoToken;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    // Attributes that can be set through URL parameters
    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    /**
     * This is used only for purpose of setup analytics
     */
    private void putSENIntoMetadata()
    {
        webResourceManager.putMetadata("SEN", jiraLicenseService.getLicense().getSupportEntitlementNumber());
    }
}
