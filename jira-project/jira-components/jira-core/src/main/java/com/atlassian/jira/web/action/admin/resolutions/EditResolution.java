/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.resolutions;

import com.atlassian.jira.config.ResolutionManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class EditResolution extends AbstractEditConstant
{
    private final ResolutionManager resolutionManager;

    public EditResolution(ResolutionManager resolutionManager)
    {
        this.resolutionManager = resolutionManager;
    }

    protected String getConstantEntityName()
    {
        return "Resolution";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.resolution.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "resolution";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getResolution(id);
    }

    protected String getRedirectPage()
    {
        return "ViewResolutions.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getResolutions();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshResolutions();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Resolution resolution = resolutionManager.getResolution(id);
        resolutionManager.editResolution(resolution, name, description);
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect(getRedirectPage());
        }
    }
}
