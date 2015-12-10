/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.fugue.Option;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * This class is used for the deleting of a permisison scheme
 */
@WebSudoRequired
public class DeletePermission extends SchemeAwarePermissionAction
{
    private Long id;
    private boolean confirmed = false;

    private final PermissionManager permissionManager;
    private final PermissionTypeManager permTypeManager;

    public DeletePermission(PermissionManager permissionManager, PermissionTypeManager permTypeManager)
    {
        this.permissionManager = permissionManager;
        this.permTypeManager = permTypeManager;
    }

    /**
     * Validates that a permission id has been passed and that the delete has been confirmed
     */
    protected void doValidation()
    {
        if (id == null)
        {
            addErrorMessage(getText("admin.errors.permissions.specify.permission.to.delete"));
        }
        if (!confirmed)
        {
            addErrorMessage(getText("admin.errors.permissions.confirm.deletion"));
        }
    }

    /**
     * Deletes the specified permission
     *
     * @return String indicating result of action
     * @throws Exception
     * @see PermissionSchemeManager
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        ManagerFactory.getPermissionSchemeManager().deleteEntity(getId());
        return getRedirect(getRedirectURL());

        /*if (getFieldLayoutId() == null)
            return getRedirect("ViewPermissionSchemes.jspa");
        else
            return getRedirect("EditPermissions!default.jspa?schemeId=" + getFieldLayoutId());*/
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets a permission object based on the id of the permission
     *
     * @return The permission object
     * @throws GenericEntityException if permission cannot be retrieved
     * @see PermissionSchemeManager
     */
    private GenericValue getPermission() throws GenericEntityException
    {
        return ManagerFactory.getPermissionSchemeManager().getEntity(id);
    }

    public String getPermissionDisplayName() throws GenericEntityException
    {
        String type = getPermission().getString("type");
        SchemeType schemeType = getType(type);
        if (schemeType != null)
        {
            return schemeType.getDisplayName();
        }
        else
        {
            return type;
        }
    }

    /**
     * Get the permission parameter. This is a value such as the group that has the permission or the current reporter
     *
     * @return The value of the parameter field of the permission object
     * @throws GenericEntityException if permission cannot be retrieved
     */
    public String getPermissionParameter() throws GenericEntityException
    {
        final GenericValue permissionGV = getPermission();
        String param = permissionGV.getString("parameter");
        String type = permissionGV.getString("type");
        SecurityType securityType = permTypeManager.getSecurityType(type);
        if (securityType != null)
        {
            return securityType.getArgumentDisplay(param);
        }
        else
        {
            return "";
        }
    }

    /**
     * Get the name of the permission
     *
     * @return The name of the permission
     * @throws GenericEntityException if permission cannot be retrieved
     */
    public String getPermissionName() throws GenericEntityException
    {
        String permissionKey = getPermission().getString("permissionKey");
        Option<ProjectPermission> permission = permissionManager.getProjectPermission(new ProjectPermissionKey(permissionKey));
        return permission.isDefined() ? permission.get().getNameI18nKey() : permissionKey;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getPermissionSchemeManager();
    }

    public String getRedirectURL()
    {
        if (getSchemeId() == null)
        {
            return "ViewPermissionSchemes.jspa";
        }
        else
        {
            return "EditPermissions!default.jspa?schemeId=" + getSchemeId();
        }
    }
}
