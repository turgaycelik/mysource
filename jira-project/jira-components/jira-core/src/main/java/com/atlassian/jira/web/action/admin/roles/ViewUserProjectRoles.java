package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

@WebSudoRequired
public class ViewUserProjectRoles extends JiraWebActionSupport
{
    protected ProjectManager projectManager;
    protected ProjectRoleService projectRoleService;
    protected ProjectFactory projectFactory;
    protected String name;
    private HashMap<Long, List<Project>> projectsByCategory;
    protected Collection<Project> currentVisibleProjects;
    private Collection<ProjectRole> projectRoles;
    private Map<GenericValue, List<Project>> visibleProjectsByCategory;
    private ApplicationUser projectRoleEditUser;
    private Map<Long, List<Long>> projectsUserInForByProjectRole;
    private ProjectRoleGroupsProjectMapper projectRoleGroupsProjectMapper;

    public ViewUserProjectRoles(final ProjectManager projectManager, final ProjectRoleService projectRoleService,
            final ProjectFactory projectFactory,  final CrowdService crowdService)
    {
        this.projectManager = projectManager;
        this.projectRoleService = projectRoleService;
        this.projectFactory = projectFactory;
    }

    public Collection<ProjectRole> getAllProjectRoles()
    {
        if (projectRoles == null)
        {
            projectRoles = projectRoleService.getProjectRoles(this);
        }
        return projectRoles;
    }

    public Collection<GenericValue> getAllProjectCategories()
    {
        return projectManager.getProjectCategories();
    }

    public Collection getAllProjectsForCategory(GenericValue projectCategory)
    {
        List<Project> projects = getProjectsByCategory().get(projectCategory.getLong("id"));
        if (projects == null)
        {
            projects = projectFactory.getProjects(projectManager.getProjectsFromProjectCategory(projectCategory));
            getProjectsByCategory().put(projectCategory.getLong("id"), projects);
        }
        return projects;
    }

    public boolean isUserInProjectRoleTypeUser(ProjectRole projectRole, Project project)
    {
        if (projectsUserInForByProjectRole == null)
        {
            projectsUserInForByProjectRole = newHashMap();
        }

        List<Long> projectsUserInForProjectRole = projectsUserInForByProjectRole.get(projectRole.getId());

        // We only ever want to perform one database query for each projectRole so we get back all the projects for
        // a given role that the userToEdit is a member of.
        if (projectsUserInForProjectRole == null)
        {
            projectsUserInForProjectRole =
                    projectRoleService.roleActorOfTypeExistsForProjects
                            (
                                    getVisibleProjectIds(), projectRole,
                                    UserRoleActorFactory.TYPE, getUserKey(), this
                            );
            projectsUserInForByProjectRole.put(projectRole.getId(), projectsUserInForProjectRole);
        }
        return projectsUserInForProjectRole.contains(project.getId());
    }

    public String getUserInProjectRoleOtherType(ProjectRole projectRole, Project project)
    {
        return getRoleMapper().getGroupNameString(projectRole, project);
    }

    public boolean isRoleForProjectSelected(ProjectRole role, Project project)
    {
        Map parameters = ActionContext.getParameters();
        String paramKey = project.getId() + "_" + role.getId();
        String paramKeyOrig = paramKey + "_orig";
        if (parameters.containsKey(paramKeyOrig))
        {
            String[] newValueParam = (String[]) parameters.get(paramKey);
            return newValueParam != null;
        }
        else
        {
            return isUserInProjectRoleTypeUser(role, project);
        }
    }

    public Collection<Project> getCurrentVisibleProjects()
    {
        if (currentVisibleProjects == null)
        {
            currentVisibleProjects = getRoleMapper().getProjects();
        }

        return currentVisibleProjects;
    }

    private ProjectRoleGroupsProjectMapper getRoleMapper()
    {
        if (projectRoleGroupsProjectMapper == null)
        {
            projectRoleGroupsProjectMapper = new ProjectRoleGroupsProjectMapper();
        }
        return projectRoleGroupsProjectMapper;
    }

    public Map getVisibleProjectsByCategory()
    {
        if (visibleProjectsByCategory == null)
        {
            visibleProjectsByCategory = new TreeMap<GenericValue, List<Project>>(OfBizComparators.NAME_COMPARATOR);
            for (Project project : getCurrentVisibleProjects())
            {
                List<Project> projects = visibleProjectsByCategory.get(project.getProjectCategory());
                if (projects == null)
                {
                    projects = newArrayList();
                    visibleProjectsByCategory.put(project.getProjectCategory(), projects);
                }
                projects.add(project);
            }
            for (List<Project> projects : visibleProjectsByCategory.values())
            {
                Collections.sort(projects, ProjectNameComparator.COMPARATOR);
            }
        }
        return visibleProjectsByCategory;
    }

    public int getProjectRoleColumnWidth()
    {
        return 75 / getAllProjectRoles().size();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ApplicationUser getProjectRoleEditUser()
    {
        if (projectRoleEditUser == null)
        {
            projectRoleEditUser = getUserManager().getUserByName(name);
        }
        return projectRoleEditUser;
    }

    private List<Long> getVisibleProjectIds()
    {
        List<Long> visibleProjectIds = newArrayList();
        for (Project project : getCurrentVisibleProjects())
        {
            visibleProjectIds.add(project.getId());
        }
        return visibleProjectIds;
    }

    private HashMap<Long, List<Project>> getProjectsByCategory()
    {
        if (projectsByCategory == null)
        {
            projectsByCategory = newHashMap();
        }
        return projectsByCategory;
    }

    public String doDefault() throws Exception
    {
        super.doDefault();
        ApplicationUser user = getProjectRoleEditUser();
        if (user == null)
        {
            this.addErrorMessage("No user exists");
            return ERROR;

        }
        return SUCCESS;
    }

    public String getReturnUrl()
    {
        String returnUrl = super.getReturnUrl();
        if (StringUtils.isEmpty(returnUrl))
        {
            return "ViewUser.jspa";
        }
        return returnUrl;
    }

    private class ProjectRoleGroupsProjectMapper
    {
        // map the roleID to a map of projectIDs that map to a lisrt of group names
        private final Map<Long, Map<Long, List<String>>> roleProjectMap = new HashMap<Long, Map<Long, List<String>>>(getAllProjectRoles().size());
        private final Set<Project> projects = newHashSet();

        ProjectRoleGroupsProjectMapper()
        {
            Set <Long> ids = newHashSet();
            for (ProjectRole projectRole : getAllProjectRoles())
            {
                // We don't want to initialize the groups by project if the user has already defined the list of
                // visible projects.
                List<Long> projectIds = (currentVisibleProjects == null) ? Collections.<Long>emptyList(): getCurrentVisibleProjectIds();
                Map<Long, List<String>> groupsByProject = projectRoleService.
                        getProjectIdsForUserInGroupsBecauseOfRole
                                (
                                        projectIds, projectRole, GroupRoleActorFactory.TYPE, getUserKey(),
                                        ViewUserProjectRoles.this
                                );

                roleProjectMap.put(projectRole.getId(), groupsByProject);
                ids.addAll(groupsByProject.keySet());
            }

            if (!ids.isEmpty())
            {
                projects.addAll(projectManager.convertToProjectObjects(ids));
            }
            projects.addAll
                    (
                            projectRoleService.getProjectsContainingRoleActorByNameAndType
                                    (
                                            getUserKey(), UserRoleActorFactory.TYPE,
                                            ViewUserProjectRoles.this
                                    )
                    );
        }

        private List<Long> getCurrentVisibleProjectIds()
        {
            List<Long> currentVisibleProjectIds = newArrayList();
            for (Project project : currentVisibleProjects)
            {
                currentVisibleProjectIds.add(project.getId());
            }
            return currentVisibleProjectIds;
        }

        String getGroupNameString(ProjectRole projectRole, Project project)
        {
            Collection<String> groupNamesUserInForProject = getGroupNames(projectRole.getId(), project.getId());

            if (groupNamesUserInForProject != null && !groupNamesUserInForProject.isEmpty())
            {
                return getListAsString(groupNamesUserInForProject);
            }
            return null;
        }

        Collection<String> getGroupNames(Long roleId, Long projectId)
        {
            Map<Long, List<String>> groupNamesByProject = roleProjectMap.get(roleId);
            if (groupNamesByProject == null)
            {
                return Collections.emptyList();
            }
            return groupNamesByProject.get(projectId);
        }

        Collection <Project> getProjects()
        {
            return projects;
        }

        String getListAsString(Collection groupNamesUserInForProject)
        {
            StringBuilder groups = new StringBuilder();
            for (Iterator iterator = groupNamesUserInForProject.iterator(); iterator.hasNext();)
            {
                groups.append((String) iterator.next());
                if (iterator.hasNext())
                {
                    groups.append(", ");
                }
            }
            return groups.toString();
        }
    }


    private String getUserKey()
    {
        ApplicationUser userObject = getProjectRoleEditUser();
        if (userObject == null)
        {
            return null;
        }
        return userObject.getKey();
    }
}
