/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class PermissionSchemeContextAccessorImpl implements PermissionSchemeAware, PermissionSchemeContextAccessor
{
    private final String[] requiredContextVariables = new String[] { JellyTagConstants.USERNAME, JellyTagConstants.PERMISSION_SCHEME_ID };
    private final Tag tag;
    private boolean hasPermissionScheme = false;
    private Long permissionSchemeId = null;

    public PermissionSchemeContextAccessorImpl(Tag tag)
    {
        this.tag = tag;
    }

    public JellyContext getContext()
    {
        return tag.getContext();
    }

    public void setPermissionScheme(Long schemeId)
    {
        setPreviousPermissionScheme();
        resetPermissionSchemeContext();
        setPermissionSchemeContext(schemeId);
    }

    public void loadPreviousPermissionScheme()
    {
        if (hasPermissionScheme)
        {
            resetPermissionSchemeContext();
            setPermissionScheme(permissionSchemeId);
            hasPermissionScheme = false;
            permissionSchemeId = null;
        }
    }

    private void setPreviousPermissionScheme()
    {
        if (hasPermissionScheme())
        {
            hasPermissionScheme = true;
            permissionSchemeId = getPermissionSchemeId();
        }
    }

    private void resetPermissionSchemeContext()
    {
        getContext().removeVariable(JellyTagConstants.PERMISSION_SCHEME_ID);
    }

    private void setPermissionSchemeContext(final Long schemeId)
    {
        getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, schemeId);
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

    public String getUsername()
    {
        return (String) getContext().getVariable(JellyTagConstants.USERNAME);
    }

    public User getUser()
    {
        return ManagerFactory.getUserManager().getUser(getUsername());
    }
}
