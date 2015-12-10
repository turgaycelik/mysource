/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Map;

@WebSudoRequired
public class AddPermission extends SchemeAwarePermissionAction
{
    private String type;
    private String[] permissions;
    private final PermissionManager permissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final PermissionTypeManager permissionTypeManager;
    private final UserKeyService userKeyService;

    public AddPermission(PermissionManager permissionManager, PermissionSchemeManager permissionSchemeManager,
            PermissionTypeManager permissionTypeManager, UserKeyService userKeyService)
    {
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.permissionTypeManager = permissionTypeManager;
        this.userKeyService = userKeyService;
    }

    protected void doValidation()
    {
        try
        {
            String permType = getType();
            SchemeType schemeType = permissionTypeManager.getSchemeType(permType);
            if (getSchemeId() == null || getScheme() == null)
            {
                addErrorMessage(getText("admin.permissions.errors.mustselectscheme"));
            }
            if (getPermissions() == null || getPermissions().length == 0)
            {
                addError("permissions", getText("admin.permissions.errors.mustselectpermission"));
            }
            if (!TextUtils.stringSet(permType))
            {
                addErrorMessage(getText("admin.permissions.errors.mustselecttype"));
            }
            else
            {
                if (schemeType != null)
                {
                    // Let the scheme type do any specific validation.
                    // It will add Error Messages to the JiraServiceContext
                    schemeType.doValidation(permType, getParameters(), getJiraServiceContext());
                }
            }
            // Check if these Permissions are valid for this SchemeType
            if (getPermissions() != null)
            {
                for (String permissionKeyStr : getPermissions())
                {
                    if (permissionKeyStr != null)
                    {
                        ProjectPermissionKey permissionKey = new ProjectPermissionKey(permissionKeyStr);
                        if (permissionManager.getProjectPermission(permissionKey).isEmpty())
                        {
                            addErrorMessage(getText("admin.permissions.errors.permissiondoesnotexist", permissionKey.permissionKey()));
                        }
                        else if (schemeType != null && !schemeType.isValidForPermission(permissionKey))
                        {
                            String permName = getPermissionName(permissionKey);
                            addErrorMessage(getText("admin.permissions.errors.invalid.combination", permName, schemeType.getDisplayName()));
                        }
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.permissions.error.occured.adding", "\n") + e.getMessage());
        }
    }

    private String getPermissionName(ProjectPermissionKey permissionKey)
    {
        Option<ProjectPermission> permission = permissionManager.getProjectPermission(permissionKey);
        return permission.isDefined() ? getText(permission.get().getNameI18nKey()) : "";
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String permissionType = getType(); //eg 'group' or 'single user'
        String parameter = getParameter(permissionType); // the group or username
        for (String permissionKeyStr : getPermissions())
        {
            ProjectPermissionKey permissionKey = new ProjectPermissionKey(permissionKeyStr);
            //if the permission already exists then dont add it again
            if (!permissionExists(permissionKey, permissionType, parameter))
            {
                SchemeEntity schemeEntity = new SchemeEntity(permissionType, parameter, permissionKey);
                permissionSchemeManager.createSchemeEntity(getScheme(), schemeEntity);
            }
        }
        return getRedirect(getRedirectURL() + getSchemeId());
    }

    private boolean permissionExists(ProjectPermissionKey permissionKey, String type, String parameter) throws GenericEntityException
    {
        return !(permissionSchemeManager.getEntities(getScheme(), permissionKey, type, parameter).isEmpty());
    }

    public Map getTypes()
    {
        return permissionTypeManager.getTypes();
    }

    /**
     * The type of the permission (eg group / single user / null for all others)
     *
     * @return the Type of the permission.
     */
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getParameters()
    {
        return ActionContext.getSingleValueParameters();
    }

    /**
     * Because we the value is set dynamically, we need to pull the correct value from the parameter chosen
     *
     * @param key 'group' / 'single user' / null
     * @return the value passed in (eg group name / user name)
     * @see #getType()
     */
    public String getParameter(String key)
    {
        String param = (String) getParameters().get(key);
        if (key.equals("user"))
        {
            // Map the username to a userkey
            param = userKeyService.getKeyForUsername(param);
        }
        return (TextUtils.stringSet(param)) ? param : null;
    }

    public String getRedirectURL()
    {
        return "EditPermissions!default.jspa?schemeId=";
    }

    public Collection<ProjectPermission> getAllPermissions()
    {
        return permissionManager.getAllProjectPermissions();
    }

    public String[] getPermissions()
    {
        return permissions;
    }

    public void setPermissions(String[] permissions)
    {
        this.permissions = permissions;
    }
}
