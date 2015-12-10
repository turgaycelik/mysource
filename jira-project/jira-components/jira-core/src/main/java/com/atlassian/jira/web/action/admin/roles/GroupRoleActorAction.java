package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.component.multigrouppicker.GroupPickerLayoutBean;
import com.atlassian.jira.web.component.multigrouppicker.GroupPickerWebComponent;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * GroupRoleActor action.
 */
public class GroupRoleActorAction extends AbstractRoleActorAction
{
    private static final String REMOVE_GROUPS_PREFIX = "removegroups_";
    private String groupNames;
    private final GroupManager groupManager;

    public GroupRoleActorAction(ProjectRoleService projectRoleService, ProjectManager projectManager, ProjectFactory projectFactory, RoleActorFactory roleActorFactory, GroupManager groupManager)
    {
        super(projectRoleService, projectManager, projectFactory, roleActorFactory);
        this.groupManager = groupManager;
    }

    protected String doExecute() throws Exception
    {
        if (!projectRoleService.hasProjectRolePermission(getProject()))
        {
            return "securitybreach";
        }

        return super.doExecute();
    }

    @RequiresXsrfCheck
    public String doRemoveGroups()
    {
        Collection<String> actorsToRemove = GroupPickerWebComponent.getGroupNamesToRemove(ActionContext.getParameters(), REMOVE_GROUPS_PREFIX);
        if (!actorsToRemove.isEmpty())
        {
            // Remove all groups that we want to remove
            if (getProject() != null)
            {
                projectRoleService.removeActorsFromProjectRole(actorsToRemove, getProjectRole(), getProject(), GroupRoleActorFactory.TYPE, this);
            }
            else
            {
                projectRoleService.removeDefaultActorsFromProjectRole(actorsToRemove, getProjectRole(), GroupRoleActorFactory.TYPE, this);
            }

            if (hasAnyErrors())
            {
                return ERROR;
            }
        }
        else
        {
            // Validate that some actors have been selected
            addErrorMessage(getText("admin.group.role.actor.action.error.remove.no.actors"));
            return ERROR;
        }
        // Clear the input params
        setGroupNames(null);
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddGroups()
    {
        final Collection<String> actorsToAdd = GroupPickerWebComponent.getGroupNamesToAdd(getGroupNames());
        if (actorsToAdd.isEmpty())
        {
            // Validate that some actors have been submitted to add
            addError("groupNames", getText("admin.group.role.actor.action.error.add.no.actors"));
        }
        else
        {
            // Add all the groups that we want to add
            if (getProject() != null)
            {
                projectRoleService.addActorsToProjectRole(actorsToAdd, getProjectRole(), getProject(), GroupRoleActorFactory.TYPE, this);
            }
            else
            {
                projectRoleService.addDefaultActorsToProjectRole(actorsToAdd, getProjectRole(), GroupRoleActorFactory.TYPE, this);
            }

            if (hasAnyErrors())
            {
                return ERROR;
            }
        }
        // Clear the input params
        setGroupNames(null);
        return SUCCESS;
    }

    public Collection getAvailableGroups()
    {
        final List<Group> groups = new ArrayList<Group>(groupManager.getAllGroups());
        groups.removeAll(getCurrentGroups());
        return groups;
    }

    public Collection getCurrentGroups()
    {
        final Collection<Group> groups = new ArrayList<Group>();
        Collection<RoleActor> roleActorsByType = null;
        if (getProject() != null)
        {
            ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(getProjectRole(), getProject(), this);
            if (projectRoleActors != null)
            {
                roleActorsByType = projectRoleActors.getRoleActorsByType(GroupRoleActorFactory.TYPE);
            }
        }
        else
        {
            DefaultRoleActors defaultRoleActors = projectRoleService.getDefaultRoleActors(getProjectRole(), this);
            if (defaultRoleActors != null)
            {
                roleActorsByType = defaultRoleActors.getRoleActorsByType(GroupRoleActorFactory.TYPE);
            }
        }
        if (roleActorsByType != null)
        {
            for (final RoleActor aRoleActorsByType : roleActorsByType)
            {
                GroupRoleActorFactory.GroupRoleActor groupRoleActor = (GroupRoleActorFactory.GroupRoleActor) aRoleActorsByType;
                groups.add(groupRoleActor.getGroup());
            }
        }
        return groups;
    }

    public String getGroupPickerHtml()
    {
        String removeGroupsAction = "GroupRoleActorAction!removeGroups.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        String addGroupAction = "GroupRoleActorAction!addGroups.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        GroupPickerLayoutBean groupPickerLayoutBean = new GroupPickerLayoutBean("admin.group.role.actor.action", REMOVE_GROUPS_PREFIX, removeGroupsAction, addGroupAction);
        GroupPickerWebComponent groupPickerWebComponent = new GroupPickerWebComponent();
        return groupPickerWebComponent.getHtml(groupPickerLayoutBean, getCurrentGroups(), true, getProjectRoleId(), EasyMap.build("valuesToAdd", getGroupNamesHtmlEncoded()));
    }

    private String getGroupNames()
    {
        return groupNames;
    }

    private String getGroupNamesHtmlEncoded()
    {
        return TextUtils.htmlEncode(getGroupNames());
    }

    public void setGroupNames(String groupNames)
    {
        this.groupNames = groupNames;
    }
}
