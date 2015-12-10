package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerLayoutBean;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerWebComponent;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Action for creating and editing UserRoleActors
 *
 * @see com.atlassian.jira.security.roles.RoleActor
 */
public class UserRoleActorAction extends AbstractRoleActorAction
{

    private static final String REMOVE_USERS_PREFIX = "removeusers_";

    private String userNames;
    private final ProjectRoleService projectRoleService;
    private final VelocityTemplatingEngine templatingEngine;
    private final UserPickerSearchService searchService;

    public UserRoleActorAction(ProjectRoleService projectRoleService, ProjectManager projectManager,
                               ProjectFactory projectFactory, RoleActorFactory roleActorFactory,
                               VelocityTemplatingEngine templatingEngine, UserPickerSearchService searchService)
    {
        super(projectRoleService, projectManager, projectFactory, roleActorFactory);
        this.projectRoleService = projectRoleService;
        this.templatingEngine = templatingEngine;
        this.searchService = searchService;
    }

    protected String doExecute()
    {
        if (!projectRoleService.hasProjectRolePermission(getProject()))
        {
            return "securitybreach";
        }
        return SUCCESS;
    }

    public String getUserPickerHtml()
    {
        String removeUsersAction = "UserRoleActorAction!removeUsers.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        String addUserAction = "UserRoleActorAction!addUsers.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        UserPickerLayoutBean userPickerLayoutBean = new UserPickerLayoutBean("admin.user.role.actor.action", REMOVE_USERS_PREFIX, removeUsersAction, addUserAction);
        UserPickerWebComponent userPickerWebComponent = new UserPickerWebComponent(templatingEngine, getApplicationProperties(), searchService);
        return userPickerWebComponent.getHtml(userPickerLayoutBean, getProjectRoleActorUsers(), true, getProjectRoleId());
    }

    /**
     * Provides the currently selected users.
     *
     * @return the users.
     */
    private Collection<User> getProjectRoleActorUsers()
    {
        DefaultRoleActors defaultRoleActors;
        if (getProject() == null)
        {
            defaultRoleActors = projectRoleService.getDefaultRoleActors(getProjectRole(), this);
        }
        else
        {
            defaultRoleActors = projectRoleService.getProjectRoleActors(getProjectRole(), getProject(), this);
        }
        SortedSet<User> usersByType = new TreeSet<User>(new UserCachingComparator(getLocale()));
        if (defaultRoleActors != null)
        {
            for (final RoleActor roleActor : defaultRoleActors.getRoleActorsByType(UserRoleActorFactory.TYPE))
            {
                ProjectRoleActor projectRoleActor = (ProjectRoleActor) roleActor;
                usersByType.add(getUserManager().getUserByKey(projectRoleActor.getParameter()).getDirectoryUser());
            }
        }
        return usersByType;
    }

    @RequiresXsrfCheck
    public String doRemoveUsers()
    {
        Collection<String> userNamesToRemove = UserPickerWebComponent.getUserNamesToRemove(ActionContext.getParameters(), REMOVE_USERS_PREFIX);
        Collection<String> userKeysToRemove = new HashSet<String>();
        for (String nameToAdd: userNamesToRemove)
        {
            ApplicationUser userToAdd = getUserManager().getUserByName(nameToAdd);
            if (userToAdd != null)
            {
                userKeysToRemove.add(userToAdd.getKey());
            }
        }

        if (getProject() == null)
        {
            projectRoleService.removeDefaultActorsFromProjectRole(userKeysToRemove, getProjectRole(), UserRoleActorFactory.TYPE, this);
        }
        else
        {
            projectRoleService.removeActorsFromProjectRole(userKeysToRemove, getProjectRole(), getProject(), UserRoleActorFactory.TYPE, this);
        }

        if (hasAnyErrors())
        {
            return ERROR;
        }

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddUsers()
    {
        Collection<String> userNamesToAdd = UserPickerWebComponent.getUserNamesToAdd(getUserNames());
        Collection<String> userKeysToAdd = new HashSet<String>();
        for (String nameToAdd: userNamesToAdd)
        {
            ApplicationUser userToAdd = getUserManager().getUserByName(nameToAdd);
            if (userToAdd != null)
            {
                userKeysToAdd.add(userToAdd.getKey());
            }
            else
            {
                getErrorMessages().add(getText("admin.user.role.actor.action.error.invalid", nameToAdd));
            }
        }

        if (getProject() == null)
        {
            projectRoleService.addDefaultActorsToProjectRole(userKeysToAdd, getProjectRole(), UserRoleActorFactory.TYPE, this);
        }
        else
        {
            projectRoleService.addActorsToProjectRole(userKeysToAdd, getProjectRole(), getProject(), UserRoleActorFactory.TYPE, this);
        }

        // do not continue if we have errors
        if (hasAnyErrors())
        {
            return ERROR;
        }

        return SUCCESS;
    }

    public String getUserNames()
    {
        return userNames;
    }

    public void setUserNames(String userNames)
    {
        this.userNames = userNames;
    }

}
