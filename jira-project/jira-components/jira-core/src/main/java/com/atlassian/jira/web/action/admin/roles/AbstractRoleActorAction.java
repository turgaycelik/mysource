package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorComparator;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA. User: detkin Date: May 26, 2006 Time: 2:19:49 PM To change this template use File |
 * Settings | File Templates.
 */
public abstract class AbstractRoleActorAction extends JiraWebActionSupport
{
    private Long projectRoleId;
    protected final ProjectRoleService projectRoleService;
    private final ProjectManager projectManager;
    private ProjectFactory projectFactory;
    protected RoleActorFactory roleActorFactory;
    private Long projectId;

    public AbstractRoleActorAction(ProjectRoleService projectRoleService, ProjectManager projectManager, ProjectFactory projectFactory, RoleActorFactory roleActorFactory)
    {
        this.projectRoleService = projectRoleService;
        this.projectManager = projectManager;
        this.projectFactory = projectFactory;
        this.roleActorFactory = roleActorFactory;
    }

    public Project getProject()
    {
        if (getProjectId() == null)
        {
            return null;
        }
        else
        {
            Project project = null;
            GenericValue projectGv = projectManager.getProject(getProjectId());
            if (projectGv != null)
            {
                project = projectFactory.getProject(projectGv);
            }
            return project;
        }
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public ProjectRole getProjectRole()
    {
        return projectRoleService.getProjectRole(projectRoleId, this);
    }

    public void setProjectRoleId(Long projectRoleId)
    {
        this.projectRoleId = projectRoleId;
    }

    public Long getProjectRoleId()
    {
        return projectRoleId;
    }

    public Collection getRoleActors(ProjectRole projectRole)
    {
        // This should only ever be null if the user does not have permission to get to this point, the UI should have
        // stopped the user from getting here
        ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(projectRole, getProject(), this);
        if (projectRoleActors == null)
        {
            return Collections.EMPTY_LIST;
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
