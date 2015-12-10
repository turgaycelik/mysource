/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

public class AddVersion extends ViewProject
{
    private final JiraAuthenticationContext authenticationContext;
    private final VersionService versionService;

    private String name;
    private String releaseDate;
    private String description;
    private Long scheduleAfterVersion;
    private VersionService.CreateVersionValidationResult validationResult;

    public AddVersion(JiraAuthenticationContext authenticationContext, VersionService versionService)
    {
        this.authenticationContext = authenticationContext;
        this.versionService = versionService;
    }

    public String doDefault() throws Exception
    {
        if (hasProjectAdminPermission() || hasAdminPermission())
        {
            return INPUT;
        }
        else
        {
            return "securitybreach";
        }
    }


    /**
     * Confirm that the entity we are about to create:
     * <ul>
     *  <li>Has a name,</li>
     *  <li>Has a case insensitivly unique name.</li>
     * </ul>
     */
    protected void doValidation()
    {
        if (!hasProjectAdminPermission())
        {
            return;
        }

        validationResult = versionService.validateCreateVersion(getLoggedInUser(), getProjectObject(), getName(), getReleaseDate(),
                getDescription(), getScheduleAfterVersion());
        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!hasProjectAdminPermission())
        {
            return "securitybreach";
        }
        versionService.createVersion(getLoggedInUser(), validationResult);

        return getRedirect("ManageVersions.jspa?pid=" + getProjectObject().getId());
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getScheduleAfterVersion()
    {
        return scheduleAfterVersion;
    }

    public void setScheduleAfterVersion(Long scheduleAfterVersion)
    {
        this.scheduleAfterVersion = scheduleAfterVersion;
    }
}
