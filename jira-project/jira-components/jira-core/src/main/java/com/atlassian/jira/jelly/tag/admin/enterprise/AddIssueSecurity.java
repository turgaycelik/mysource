/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin.enterprise;

import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.enterprise.IssueSchemeLevelAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

public class AddIssueSecurity extends IssueSchemeLevelAwareActionTagSupport
{
    private static final String SCHEME_ID = "schemeId";
    private static final String SECURITY_ID = "security";
    private static final String SECURITY_TYPE = "type";

    public AddIssueSecurity()
    {
        setActionName("AddIssueSecurity");
        ignoreErrors = false;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(SCHEME_ID, getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_ID).toString());
        setProperty(SECURITY_ID, getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID).toString());
    }

    public String[] getRequiredProperties()
    {
        return new String[] { SCHEME_ID, SECURITY_ID, SECURITY_TYPE };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }
}
