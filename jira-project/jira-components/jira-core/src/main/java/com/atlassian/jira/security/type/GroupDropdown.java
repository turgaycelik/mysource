/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserUtil;
import com.google.common.collect.Sets;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

public class GroupDropdown extends AbstractProjectsSecurityType
{
    public static final String DESC = "group";
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public GroupDropdown(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.group");
    }

    public String getType()
    {
        return DESC;
    }

    public List getGroups()
    {
        Collection<Group> groups = ComponentAccessor.getUserManager().getGroups();
        if (groups instanceof List)
        {
            return (List<Group>) groups;
        }
        ArrayList<Group> groupsAsList = new ArrayList<Group>();
        groupsAsList.addAll(groups);
        return groupsAsList;
    }

    /**
     * If there is no user passed to a security type then they have the permission if there is no group set
     *
     * @param entity - not used
     * @param group  The group. If it is null then it is Anyone
     * @see CurrentAssignee#hasPermission
     * @see CurrentReporter#hasPermission
     * @see ProjectLead#hasPermission
     * @see SingleUser#hasPermission
     * @see GroupDropdown#hasPermission
     */
    @Override
    public boolean hasPermission(GenericValue entity, String group)
    {
        return (group == null);
    }

    @Override
    public boolean hasPermission(Project project, String group)
    {
        // If there is no user passed to this security type then they have the permission if there is no group set
        return (group == null);
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter)
    {
        // If there is no user passed to this security type then they have the permission if there is no group set
        return (parameter == null);
    }

    /**
     * Determines if the use is part of a group that has the permission
     *
     * @param entity        not used
     * @param groupName         The group. If it is null then it is Anyone
     * @param user          The user to check if they are in the group, must not be null
     * @param issueCreation not used
     * @see com.atlassian.jira.security.type.CurrentAssignee#hasPermission
     * @see com.atlassian.jira.security.type.CurrentReporter#hasPermission
     * @see ProjectLead#hasPermission
     * @see SingleUser#hasPermission
     */
    @Override
    public boolean hasPermission(GenericValue entity, String groupName, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User passed must not be null");
        }
        //If there is no group then it is Anyone so it is true
        if (hasPermission(entity, groupName))
        {
            return true;
        }
        CrowdService crowdService = ComponentAccessor.getComponentOfType(CrowdService.class);
        Group group = crowdService.getGroup(groupName);
        if (group == null)
        {
            return false;
        }
        return crowdService.isUserMemberOfGroup(user, group);

    }

    @Override
    public boolean hasPermission(Project project, String parameter, User user, boolean issueCreation)
    {
        return hasPermission(parameter, user);
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter, User user, boolean issueCreation)
    {
        return hasPermission(parameter, user);
    }

    private boolean hasPermission(String groupName, User user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User passed must not be null");
        }
        // If there is no group then it is Anyone so it is true
        if (groupName == null)
        {
            return true;
        }
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        return groupManager.isUserInGroup(user.getName(), groupName);
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    public Set<User> getUsers(PermissionContext ctx, String groupName)
    {
        if (groupName == null)
        {
            return Sets.newHashSet(UserUtils.getAllUsers());
        }

        return Sets.newHashSet(getUserUtil().getAllUsersInGroupNamesUnsorted(singletonList(groupName)));
    }

    private UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }
}
