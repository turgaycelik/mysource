package com.atlassian.jira.permission;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

import java.util.List;

/**
 * An internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectPermissionSchemeHelper
{
    /**
     * Gets the projects using a given Permission {@link Scheme}. Similar to
     * {@link PermissionSchemeManager#getProjects(com.atlassian.jira.scheme.Scheme)}, but only projects for which
     * the requesting user has {@link com.atlassian.jira.bc.project.ProjectAction#EDIT_PROJECT_CONFIG} permissions.
     *
     * @param permissionScheme permissionScheme to find associated projects for.
     * @return list of projects which use the given permissionScheme. Sorted by {@link com.atlassian.jira.issue.comparator.ProjectNameComparator#COMPARATOR}.
     */
    public List<Project> getSharedProjects(Scheme permissionScheme);
}
