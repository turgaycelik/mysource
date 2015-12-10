/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin.enterprise;

import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class CreateIssueSecurityScheme extends UserAwareActionTagSupport
{
    private static final Logger log = Logger.getLogger(CreateIssueSecurityScheme.class);
    private static final String KEY_SCHEMEID = "schemeId";
    private boolean hasPreviousIssueSchemeId = false;
    private Long previousIssueSchemeId = null;

    public CreateIssueSecurityScheme()
    {
        setActionName("AddIssueSecurityScheme");
        ignoreErrors = false;
    }

    protected void postTagExecution(XMLOutput output)
    {
        log.debug("CreatePermissionScheme.postTagExecution");
        copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());

        try
        {
            setPreviousIssueSchemeId((Long) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_ID));
            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_ID, new Long(getProperty(KEY_SCHEMEID)));
        }
        catch (NumberFormatException e)
        {
            log.error(e, e);
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        if (hasPreviousIssueSchemeId)
            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_ID, getPreviousIssueSchemeId());
    }

    public String[] getRequiredProperties()
    {
        return new String[0];
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.ISSUE_SCHEME_ID };
    }

    public Long getPreviousIssueSchemeId()
    {
        return previousIssueSchemeId;
    }

    public void setPreviousIssueSchemeId(Long previousIssueSchemeId)
    {
        this.hasPreviousIssueSchemeId = true;
        this.previousIssueSchemeId = previousIssueSchemeId;
    }
}
