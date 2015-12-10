package com.atlassian.jira.notification;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

import java.util.List;

/**
 * An internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectNotificationsSchemeHelper
{
    /**
     * Gets the projects using a given Notifications {@link Scheme}. Similar to
     * {@link NotificationSchemeManager#getProjects(com.atlassian.jira.scheme.Scheme)}, but only projects for which
     * the requesting user has {@link com.atlassian.jira.bc.project.ProjectAction#EDIT_PROJECT_CONFIG} permissions.
     *
     * @param notificationsScheme notificationsScheme to find associated projects for.
     * @return list of projects which use the given notificationsScheme. Sorted by {@link com.atlassian.jira.issue.comparator.ProjectNameComparator#COMPARATOR}
     */

    public List<Project> getSharedProjects(Scheme notificationsScheme);
}
