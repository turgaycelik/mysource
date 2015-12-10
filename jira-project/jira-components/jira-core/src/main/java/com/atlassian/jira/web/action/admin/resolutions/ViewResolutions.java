/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.resolutions;

import com.atlassian.jira.config.ResolutionManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class ViewResolutions extends AbstractViewConstants
{
    private final ResolutionManager resolutionManager;

    public ViewResolutions(final TranslationManager translationManager, ResolutionManager resolutionManager)
    {
        super(translationManager);
        this.resolutionManager = resolutionManager;
    }

    protected String getConstantEntityName()
    {
        return "Resolution";
    }

    protected String getNiceConstantName()
    {
        return "resolution";
    }

    protected String getIssueConstantField()
    {
        return getText("admin.issue.constant.resolution.lowercase");
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

    protected String redirectToView()
    {
        return getRedirect("ViewResolutions.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION;
    }

    @Override
    protected GenericValue addConstant() throws GenericEntityException
    {
        Resolution resolution = resolutionManager.createResolution(name, description);
        return resolution.getGenericValue();
    }

    public String doMakeDefault() throws Exception
    {
        resolutionManager.setDefaultResolution(make);
        return SUCCESS;
    }

    public String doMoveDown()
    {
        resolutionManager.moveResolutionDown(down);
        return getResult();
    }

    public String doMoveUp()
    {
        resolutionManager.moveResolutionUp(up);
        return getResult();
    }
}
