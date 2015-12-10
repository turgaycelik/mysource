/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.jelly.PermissionSchemeContextAccessor;
import com.atlassian.jira.jelly.PermissionSchemeContextAccessorImpl;
import com.atlassian.jira.jelly.tag.PermissionSchemeAwareActionTagSupport;
import com.atlassian.jira.security.Permissions;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.util.StringUtil;

import java.util.List;

public class AddPermission extends PermissionSchemeAwareActionTagSupport implements PermissionSchemeContextAccessor
{
    private static final transient Logger log = Logger.getLogger(AddPermission.class);
    private static final String KEY_SCHEME_ID = "schemeId";
    private static final String KEY_PERMISSION_TYPE = "permissions";
    private static final String KEY_GROUP_NAME = "group";
    private static final String KEY_ROLE_NAME = "projectrole";
    private static final String KEY_ROLE_ID = "projectroleid";
    private static final String KEY_TYPE = "type";
    private PermissionSchemeContextAccessor permissionSchemeContextAccessor = new PermissionSchemeContextAccessorImpl(this);

    public AddPermission()
    {
        setActionName("AddPermission");
        ignoreErrors = true;
    }

    protected void preContextValidation()
    {
        super.preContextValidation();
        if (getProperties().containsKey(KEY_SCHEME_ID))
        {
            setPermissionScheme(new Long(getProperty(KEY_SCHEME_ID)));
        }
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(KEY_SCHEME_ID, getPermissionSchemeId().toString());

        // Map permission value
        List permissionsStrings = StringUtil.split(getProperty(KEY_PERMISSION_TYPE), ",");
        String[] permissionsIds = new String[permissionsStrings.size()];
        for (int i = 0; i < permissionsStrings.size(); i++)
        {
            permissionsIds[i] = Integer.toString(Permissions.getType((String) permissionsStrings.get(i)));
        }
        setProperty(KEY_PERMISSION_TYPE, permissionsIds);

        if (!getProperties().containsKey(KEY_TYPE))
        {
            setProperty(KEY_TYPE, "group");
        }

        if (!getProperties().containsKey(KEY_GROUP_NAME))
        {
            setProperty(KEY_GROUP_NAME, "");
        }

        // Map projectroleid to projectrole as expected by action
        if (getProperties().containsKey(KEY_ROLE_ID))
        {
            setProperty(KEY_ROLE_NAME, getProperty(KEY_ROLE_ID));
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousPermissionScheme();
    }

    public String[] getRequiredProperties()
    {
        if ("group".equals(getProperty(KEY_TYPE)))
        {
            return new String[]{KEY_SCHEME_ID, KEY_PERMISSION_TYPE, KEY_TYPE, KEY_GROUP_NAME};
        }
        else if ("projectrole".equals(getProperty(KEY_TYPE)))
        {
            return new String[]{KEY_SCHEME_ID, KEY_PERMISSION_TYPE, KEY_TYPE, KEY_ROLE_ID};
        }
        else
        {
            return new String[]{KEY_SCHEME_ID, KEY_PERMISSION_TYPE, KEY_TYPE};
        }
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public void setPermissionScheme(Long schemeId)
    {
        permissionSchemeContextAccessor.setPermissionScheme(schemeId);
    }

    public void loadPreviousPermissionScheme()
    {
        permissionSchemeContextAccessor.loadPreviousPermissionScheme();
    }
}
