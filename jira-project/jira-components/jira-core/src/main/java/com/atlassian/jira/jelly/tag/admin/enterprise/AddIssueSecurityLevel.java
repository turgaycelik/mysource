/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin.enterprise;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.enterprise.IssueSchemeAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class AddIssueSecurityLevel extends IssueSchemeAwareActionTagSupport
{
    private static final Logger log = Logger.getLogger(AddIssueSecurityLevel.class);
    private static final String ISSUE_SECURITY_LEVEL_NAME = "name";
    private static final String ISSUE_SECURITY_LEVEL_DESCRIPTION = "description";
    private boolean hasPreviousIssueSecurityLevel = false;
    private Long previousIssueSecurityLevelId = null;

    public AddIssueSecurityLevel()
    {
        setActionName("EditIssueSecurities!addLevel");
        ignoreErrors = false;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (hasIssueScheme())
            setProperty("schemeId", getIssueSchemeId().toString());
    }

    protected void postTagExecution(XMLOutput output)
    {
        log.debug("CreatePermissionScheme.postTagExecution");
        copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());

        //Retrieve the issue level from the scheme and add id in to the context.
        final List<GenericValue> levels = ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(getIssueSchemeId());
        for (GenericValue genericValue : levels)
        {
            if (getProperty(ISSUE_SECURITY_LEVEL_NAME).equals(genericValue.getString("name")))
            {
                setPreviousIssueSecurityLevelId((Long) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID));
                getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID, genericValue.getLong("id"));
                return;
            }
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        if (hasPreviousIssueSecurityLevel)
            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID, getPreviousIssueSecurityLevelId());
    }

    public String[] getRequiredProperties()
    {
        return new String[] { ISSUE_SECURITY_LEVEL_NAME, ISSUE_SECURITY_LEVEL_DESCRIPTION };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.ISSUE_SCHEME_LEVEL_ID };
    }

    public Long getPreviousIssueSecurityLevelId()
    {
        return previousIssueSecurityLevelId;
    }

    public void setPreviousIssueSecurityLevelId(Long previousIssueSecurityLevelId)
    {
        this.hasPreviousIssueSecurityLevel = true;
        this.previousIssueSecurityLevelId = previousIssueSecurityLevelId;
    }
}
