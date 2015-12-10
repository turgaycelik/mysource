/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.jira.crowd.embedded.JaacsService;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebSudoRequired
public class ConfigureCrowdServer extends JiraWebActionSupport
{
    /**
     * The name of this action.
     */
    static final String ACTION = "ConfigureCrowdServer";

    // dependencies
    private final JaacsService jaacsService;

    // properties
    private String success;

    public ConfigureCrowdServer(ApplicationManager applicationManager, JaacsService jaacsService)
    {
        this.jaacsService = jaacsService;
    }

    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    /**
     * Returns the applications list, sorted by name.
     *
     * @return a List of Application
     */
    public List<Application> getApplications()
    {
        List<Application> applications = jaacsService.findAll(getLoggedInUser());

        // return a sorted list
        Collections.sort(applications, new ApplicationNameComparator());
        return applications;
    }

    public String getSuccess()
    {
        return success;
    }

    public void setSuccess(String success)
    {
        this.success = success;
    }

    /**
     * Compare applications by name.
     */
    protected static class ApplicationNameComparator implements Comparator<Application>
    {
        @Override
        public int compare(Application app1, Application app2)
        {
            return app1.getName().compareTo(app2.getName());
        }
    }
}
