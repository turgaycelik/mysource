package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorComparator;
import com.atlassian.plugin.PluginAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ViewProjectRoleActors extends AbstractRoleActors
{
    private final ProjectRoleService projectRoleService;
    private final ProjectManager projectManager;
    private Long projectId;

    public ViewProjectRoleActors(ProjectRoleService projectRoleService, ProjectManager projectManager, ProjectFactory projectFactory, PluginAccessor pluginAccessor)
    {
        super(projectRoleService, pluginAccessor);
        this.projectRoleService = projectRoleService;
        this.projectManager = projectManager;
        final ProjectFactory projectFactory1 = projectFactory;
    }

    protected String doExecute() throws Exception
    {
        if (!projectRoleService.hasProjectRolePermission(getProject()))
        {
            return "securitybreach";
        }
        return super.doExecute();
    }

    /**
     * Returns the RoleActors in a {@link ProjectRole} that have the given type
     * e.g. {@link com.atlassian.jira.security.roles.actor.UserRoleActorFactory.UserRoleActor#}
     * or {@link com.atlassian.jira.security.roles.actor.GroupRoleActorFactory.GroupRoleActor#}.
     * @param projectRole
     * @param type designation to specify which kind of RoleActor to get.
     * @param length
     * @return the Collection of {@link com.atlassian.jira.security.roles.RoleActor}s.
     */
    public Collection getRoleActorTypes(ProjectRole projectRole, String type, int length)
    {
        ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(projectRole, getProject(), this);

        // This should only ever be null if the user does not have permission to get to this point, the UI should have
        // stopped the user from getting here
        if (projectRoleActors == null)
        {
            return Collections.EMPTY_LIST;
        }

        Set<RoleActor> roleActorsByType = projectRoleActors.getRoleActorsByType(type);
        if (length == -1 || roleActorsByType.size() <= length)
        {
            return projectRoleActors.getRoleActorsByType(type);
        }
        else
        {
            // Sublist the return set via an ArrayList (Sorry!)
            return new ArrayList<RoleActor>(projectRoleActors.getRoleActorsByType(type)).subList(0,length);
        }
    }

    public Project getProject()
    {
        return projectManager.getProjectObj(getProjectId());
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    //---------------- Methods for editing project roles -----------------//

    public Collection getRoleActors(ProjectRole projectRole)
    {
        // This should only ever be null if the user does not have permission to get to this point, the UI should have
        // stopped the user from getting here
         ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(projectRole, getProject(), this);
        if (projectRoleActors == null)
        {
            return Collections.emptyList();
        }
        Set<RoleActor> roleActors = projectRoleActors.getRoleActors();
        SortedSet<RoleActor> sortedRoleActors = new TreeSet<RoleActor>(RoleActorComparator.COMPARATOR);
        if (roleActors != null)
        {
            sortedRoleActors.addAll(roleActors);
        }
        return sortedRoleActors;
    }

}