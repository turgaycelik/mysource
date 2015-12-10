package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Convienience wrapper for the {@link com.atlassian.jira.user.UserHistoryManager} that deals directly with Projects.
 *
 * @since v4.0
 */
public class DefaultUserProjectHistoryManager implements UserProjectHistoryManager
{
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final UserHistoryManager userHistoryManager;

    public DefaultUserProjectHistoryManager(UserHistoryManager userHistoryManager, ProjectManager projectManager,
                                            PermissionManager permissionManager)
    {
        this.userHistoryManager = userHistoryManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    public void addProjectToHistory(User user, Project project)
    {
        notNull("project", project);
        userHistoryManager.addItemToHistory(UserHistoryItem.PROJECT, user, project.getId().toString());
    }

    public boolean hasProjectHistory(int permission, User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.PROJECT, user);
        if (history != null)
        {
            for (final UserHistoryItem historyItem : history)
            {
                final Project project = projectManager.getProjectObj(Long.valueOf(historyItem.getEntityId()));
                if (project != null && permissionManager.hasPermission(permission, project, user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Project getCurrentProject(int permission, User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.PROJECT, user);
        if (history != null)
        {
            for (final UserHistoryItem historyItem : history)
            {
                final Project project = projectManager.getProjectObj(Long.valueOf(historyItem.getEntityId()));
                if (project != null && permissionManager.hasPermission(permission, project, user))
                {
                    return project;
                }
            }
        }
        return null;
    }

    public List<UserHistoryItem> getProjectHistoryWithoutPermissionChecks(User user)
    {
        return userHistoryManager.getHistory(UserHistoryItem.PROJECT, user);
    }

    public List<Project> getProjectHistoryWithPermissionChecks(int permission, User user)
    {
        final List<UserHistoryItem> history = getProjectHistoryWithoutPermissionChecks(user);
        final List<Project> returnList = new ArrayList<Project>();

        if (history != null)
        {
            for (UserHistoryItem userHistoryItem : history)
            {
                final Project project = projectManager.getProjectObj(Long.valueOf(userHistoryItem.getEntityId()));
                if (project != null && permissionManager.hasPermission(permission, project, user))
                {
                    returnList.add(project);
                }
            }
        }
        return returnList;
    }

    public List<Project> getProjectHistoryWithPermissionChecks(ProjectAction projectAction, User user)
    {
        final List<UserHistoryItem> history = getProjectHistoryWithoutPermissionChecks(user);
        final List<Project> returnList = new ArrayList<Project>();

        if (history != null)
        {
            for (UserHistoryItem userHistoryItem : history)
            {
                final Project project = projectManager.getProjectObj(Long.valueOf(userHistoryItem.getEntityId()));

                if (project != null && projectAction.hasPermission(permissionManager, user, project))
                {
                    returnList.add(project);
                }
            }
        }
        return returnList;
    }
}
