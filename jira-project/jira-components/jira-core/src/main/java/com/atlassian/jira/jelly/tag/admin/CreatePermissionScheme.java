/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class CreatePermissionScheme extends UserAwareActionTagSupport
{
    private static final Logger log = Logger.getLogger(CreatePermissionScheme.class);
    private static final String KEY_SCHEMENAME = "name";
    private static final String KEY_SCHEMEID = "schemeId";
    private boolean hasPreviousPermissionSchemeId = false;
    private Long previousPermissionSchemeId = null;

    public CreatePermissionScheme()
    {
        setActionName("AddPermissionScheme");
    }

    protected void postTagExecution(XMLOutput output)
    {
        log.debug("CreatePermissionScheme.postTagExecution");
        copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());

        try
        {
            setPreviousPermissionSchemeId((Long) getContext().getVariable(JellyTagConstants.PERMISSION_SCHEME_ID));
            getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, new Long(getProperty(KEY_SCHEMEID)));
        }
        catch (NumberFormatException e)
        {
            log.error(e, e);
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        if (hasPreviousPermissionSchemeId)
            getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, getPreviousPermissionSchemeId());
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_SCHEMENAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[] { JellyTagConstants.PERMISSION_SCHEME_ID };
    }

    public Long getPreviousPermissionSchemeId()
    {
        return previousPermissionSchemeId;
    }

    public void setPreviousPermissionSchemeId(Long previousPermissionSchemeId)
    {
        this.hasPreviousPermissionSchemeId = true;
        this.previousPermissionSchemeId = previousPermissionSchemeId;
    }
}
