/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.IssueActionSupport;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class DeleteProjectEntity extends IssueActionSupport
{
    private Long pid;

    public String doDefault() throws Exception
    {
        if (hasPermission())
            return super.doDefault();
        else
            return "securitybreach";
    }

    protected boolean hasPermission() throws Exception
    {
        final PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        return permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()) || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, getProject(), getLoggedInUser());
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public GenericValue getProject() throws GenericEntityException
    {
        return null;
    }
}
