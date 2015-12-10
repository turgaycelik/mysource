package com.atlassian.jira.bc.scheme.mapper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.scheme.mapper.SchemeGroupsToRolesTransformer;
import com.atlassian.jira.scheme.mapper.SchemeTransformResults;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: detkin Date: Jun 23, 2006 Time: 3:50:27 PM To change this template use File |
 * Settings | File Templates.
 */
public class DefaultSchemeGroupsToRoleTransformerService implements SchemeGroupsToRoleTransformerService
{
    private SchemeGroupsToRolesTransformer schemeGroupsToRolesTransformer;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private GlobalPermissionManager globalPermissionManager;

    public DefaultSchemeGroupsToRoleTransformerService(SchemeGroupsToRolesTransformer schemeGroupsToRolesTransformer, PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext, GlobalPermissionManager globalPermissionManager)
    {
        this.schemeGroupsToRolesTransformer = schemeGroupsToRolesTransformer;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.globalPermissionManager = globalPermissionManager;
    }

    public SchemeTransformResults doTransform(User currentUser, List schemes, Set groupRoleMappings, ErrorCollection errorCollection)
    {
        if (schemes == null)
        {
            // Add an error
        }
        if (hasAdminPermission(currentUser))
        {
            return schemeGroupsToRolesTransformer.doTransform(schemes, groupRoleMappings);
        }
        else
        {
            // TODO: make this relevant to this class
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return null;
    }

    public void persistTransformationResults(User currentUser, SchemeTransformResults schemeTransformResults, ErrorCollection errorCollection)
    {
        if (schemeTransformResults == null)
        {
            // Add an error
        }
        if (hasAdminPermission(currentUser))
        {
            schemeGroupsToRolesTransformer.persistTransformationResults(schemeTransformResults);
        }
        else
        {
            // TODO: make this relevant to this class
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
    }

    public boolean isGroupGrantedGlobalUsePermission(String groupName)
    {
        return globalPermissionManager.getGroupNames(Permissions.USE).contains(groupName);
    }

    public boolean isAnyGroupGrantedGlobalUsePermission(Collection groupNames)
    {
        Collection groupsWithGlobalUsePermission = getGroupsWithGlobalUsePermission(groupNames);
        return !groupsWithGlobalUsePermission.isEmpty();
    }

    public Collection getGroupsWithGlobalUsePermission(Collection groups)
    {
        List<String> groupsWithGlobalUsePermission = new ArrayList<String>();

        for (final Object group : groups)
        {
            String groupName = (String) group;
            if (isGroupGrantedGlobalUsePermission(groupName))
            {
                groupsWithGlobalUsePermission.add(groupName);
            }
        }

        return groupsWithGlobalUsePermission;
    }

    public Collection getGroupsWithoutGlobalUsePermission(Collection groups)
    {
        
        List<String> groupsWithoutGlobalUsePermission = new ArrayList<String>();
        for (final Object group : groups)
        {
            String groupName = (String) group;
            if (!isGroupGrantedGlobalUsePermission(groupName))
            {
                groupsWithoutGlobalUsePermission.add(groupName);
            }
        }

        return groupsWithoutGlobalUsePermission;
    }

    private boolean hasAdminPermission(User currentUser)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }
}
