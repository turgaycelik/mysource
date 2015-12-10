/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.PermissionSchemeAware;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public abstract class AssignPermissionScheme extends ProjectAwareActionTagSupport implements PermissionSchemeAware
{
    private static final transient Logger log = Logger.getLogger(AssignPermissionScheme.class);
    private static final String KEY_SCHEME_ID_LIST = "schemeIds";
    private static final String KEY_PROJECT_ID = "projectId";

    public AssignPermissionScheme()
    {
        setActionName("SelectProjectScheme");
    }

    public String[] getRequiredContextVariables()
    {
        String[] result = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, result, 0, super.getRequiredContextVariables().length);
        result[result.length - 1] = JellyTagConstants.PERMISSION_SCHEME_ID;
        return result;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(KEY_PROJECT_ID, getProjectId().toString());
        setProperty(KEY_SCHEME_ID_LIST, getPermissionSchemeId().toString());
    }

    public boolean hasPermissionScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.PERMISSION_SCHEME_ID);
    }

    public Long getPermissionSchemeId()
    {
        final String permissionSchemeIdStr = (String) getContext().getVariable(JellyTagConstants.PERMISSION_SCHEME_ID);
        try
        {
            return new Long(permissionSchemeIdStr);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public GenericValue getPermissionScheme()
    {
        try
        {
            return ManagerFactory.getPermissionSchemeManager().getScheme(getPermissionSchemeId());
        }
        catch (GenericEntityException e)
        {
            return null;
        }
    }
}
