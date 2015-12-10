/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.util;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.security.PermissionManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupToPermissionSchemeMapper extends AbstractGroupToSchemeMapper
{
    private final PermissionManager permissionManager;

    public GroupToPermissionSchemeMapper(PermissionSchemeManager permissionSchemeManager, PermissionManager permissionManager) throws GenericEntityException
    {
        super(permissionSchemeManager);
        this.permissionManager = permissionManager;
        setGroupMapping(realInit());
    }

    /** Nasty hacks to get around the dependencies problem **/
    protected Map init() throws GenericEntityException
    {
        return null;
    }

    /**
     * Go through all the Permission Schemes and create a Map, where the key is the group name
     * and values are Sets of Schemes
     */
    protected Map realInit() throws GenericEntityException
    {
        Map mapping = new HashMap();

        // Get all Permission Schmes
        final List<GenericValue> schemes = getSchemeManager().getSchemes();
        for (GenericValue permissionScheme : schemes)
        {
            // For each scheme get all the permissions
            for (ProjectPermission permission : permissionManager.getAllProjectPermissions())
            {
                // Get all the groups for this permission
                final List<GenericValue> entities = getSchemeManager().getEntities(permissionScheme, permission.getKey());
                for (GenericValue permissionRecord : entities)
                {
                    if ("group".equals(permissionRecord.getString("type")))
                    {
                        addEntry(mapping, permissionRecord.getString("parameter"), permissionScheme);
                    }
                }
            }
        }

        return mapping;
    }
}
