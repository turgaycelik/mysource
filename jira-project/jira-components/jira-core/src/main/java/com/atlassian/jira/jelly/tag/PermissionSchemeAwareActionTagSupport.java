/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.PermissionSchemeAware;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public abstract class PermissionSchemeAwareActionTagSupport extends UserAwareActionTagSupport implements PermissionSchemeAware
{
    private final String[] requiredContextVariables;

    public PermissionSchemeAwareActionTagSupport()
    {
        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = JellyTagConstants.PERMISSION_SCHEME_ID;
        requiredContextVariables = temp;
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public boolean hasPermissionScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.PERMISSION_SCHEME_ID);
    }

    public Long getPermissionSchemeId()
    {
        return (Long) getContext().getVariable(JellyTagConstants.PERMISSION_SCHEME_ID);
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
