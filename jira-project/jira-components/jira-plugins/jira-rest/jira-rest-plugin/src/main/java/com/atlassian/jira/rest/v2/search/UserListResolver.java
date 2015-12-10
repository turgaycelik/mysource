package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Resolves users for given FilterPermissionBeans.
 *
 * @since v6.0
 */
class UserListResolver
{
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;
    private final SchemeManager schemeManager;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;
    final Collection<FilterPermissionBean> permissions;

    public UserListResolver(final JiraAuthenticationContext authContext, final UserManager userManager, final GroupManager groupManager,
            final ProjectManager projectManager, final PermissionManager permissionManager, final ProjectRoleManager projectRoleManager,
            final SchemeManager schemeManager, final Collection<FilterPermissionBean> permissions)
    {
        this.authContext = authContext;
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.schemeManager = schemeManager;
        this.permissions = permissions;
    }

    public Collection<User> getShareUsers()
    {
        final ImmutableSet.Builder<User> sharedUsersBuilder = ImmutableSet.builder();
        // need browse user permission
        if (permissionManager.hasPermission(Permissions.USER_PICKER, authContext.getUser()))
        {
            for (final FilterPermissionBean sharePermission : permissions)
            {
                final String type = sharePermission.getType();
                if (type.equals(ShareType.Name.GLOBAL.toString()))
                {
                    // we've just got all users, they are already a unique set, so just return them.
                    return ImmutableList.copyOf(getAllActiveUsers());
                }
                else if (type.equals(ShareType.Name.GROUP.toString()))
                {
                    final GroupJsonBean groupJsonBean = sharePermission.getGroup();
                    final Group group = groupManager.getGroup(groupJsonBean.getName());
                    sharedUsersBuilder.addAll(activeUsers(groupManager.getUsersInGroup(group)));
                }
                else if (type.equals(ShareType.Name.PROJECT.toString()))
                {
                    final ProjectBean projectBean = sharePermission.getProject();
                    final Project project = projectManager.getProjectObjByName(projectBean.getName());

                    final ProjectRoleBean roleBean = sharePermission.getRole();
                    if (roleBean == null)
                    {
                        // No role bean means anyone with BROWSE permission in that project
                        sharedUsersBuilder.addAll(activeUsers(schemeManager.getUsers((long) Permissions.BROWSE, project)));
                    }
                    else
                    {
                        // A single role specified so find the users in that role.
                        final ProjectRole projectRole = projectRoleManager.getProjectRole(roleBean.name);
                        sharedUsersBuilder.addAll(activeUsers(projectRoleManager.getProjectRoleActors(projectRole, project).getUsers()));
                    }
                }
                else
                {
                    throw new IllegalStateException("Unknown share type of: " + type);
                }
            }
        }
        return sharedUsersBuilder.build();
    }

    private Iterable<User> getAllActiveUsers()
    {
        return activeUsers(userManager.getUsers());
    }


    Iterable<User> activeUsers(final Collection<User> users)
    {
        return Iterables.filter(users, new Predicate<User>()
        {
            @Override
            public boolean apply(@Nullable User user)
            {
                return user != null && user.isActive();
            }
        });
    }

}
