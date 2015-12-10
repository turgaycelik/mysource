package com.atlassian.jira.issue.security;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;


import java.util.List;

/**
 * An internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectIssueSecuritySchemeHelper
{
    /**
     * Gets the projects using a given IssueSecurity {@link Scheme}. Similar to
     * {@link IssueSecuritySchemeManager#getProjects(com.atlassian.jira.scheme.Scheme)}, but only projects for which
     * the requesting user has {@link com.atlassian.jira.bc.project.ProjectAction#EDIT_PROJECT_CONFIG} permissions.
     *
     * @param issueSecurityScheme issueSecurityScheme to find associated projects for.
     * @return list of projects which use the given issueSecurityScheme. Sorted by {@link com.atlassian.jira.issue.comparator.ProjectNameComparator#COMPARATOR}
     */

    public List<Project> getSharedProjects(Scheme issueSecurityScheme);
}
