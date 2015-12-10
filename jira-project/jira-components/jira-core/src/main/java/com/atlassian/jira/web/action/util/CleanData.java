/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import webwork.action.ServletActionContext;

public class CleanData extends JiraWebActionSupport
{
    private final JiraProperties jiraSystemProperties;
    private String userName;
    private String password;
    private boolean dataCleaned;
    private final CrowdService crowdService;

    public CleanData(CrowdService crowdService, final JiraProperties jiraSystemProperties)
    {
        this.crowdService = crowdService;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    public String doDefault() throws Exception
    {
        dataCleaned = false;
        return SUCCESS;
    }

    protected void doValidation()
    {
        //check that the user is an admin and that the password is correct
        User user = crowdService.getUser(userName);
        if (user != null)
        {
            try
            {
                crowdService.authenticate(user.getName(), password);
            }
            catch (FailedAuthenticationException e)
            {
                addErrorMessage(getText("admin.errors.cleandata.username.password.incorrect"));
                return;
            }

            if (!nonAdminUpgradeAllowed())
            {
                boolean hasAdminPermission = ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, user);

                if (!hasAdminPermission)
                {
                    addError("userName", getText("admin.errors.cleandata.no.admin.permission"));
                }
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.cleandata.username.password.incorrect"));
        }
    }

    protected String doExecute() throws Exception
    {
        OfBizDelegator ofBizDelegator = ComponentAccessor.getComponent(OfBizDelegator.class);
        ApplicationProperties applicationProperties = ComponentAccessor.getComponent(ApplicationProperties.class);
        DataCleaner dataCleaner = new DataCleaner(applicationProperties, ofBizDelegator);
        dataCleaner.clean();

        // Lock JIRA so the server has to be restarted and
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        for (final Object o : cont.getEvents())
        {
            Event event = (Event) o;
            if (event != null && event.getKey().equals(EventType.get("export-illegal-xml")))
            {
                cont.removeEvent(event);
            }
        }

        Event newEvent = new Event(EventType.get("restart"), "The illegal XML characters have been removed. The server needs to be restarted.", EventLevel.get(EventLevel.ERROR));
        cont.addEvent(newEvent);

        setDataCleaned(true);

        return getResult();
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    private boolean nonAdminUpgradeAllowed()
    {
        return jiraSystemProperties.getBoolean(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY);
    }

    public boolean isDataCleaned()
    {
        return dataCleaned;
    }

    private void setDataCleaned(boolean dataCleaned)
    {
        this.dataCleaned = dataCleaned;
    }
}
