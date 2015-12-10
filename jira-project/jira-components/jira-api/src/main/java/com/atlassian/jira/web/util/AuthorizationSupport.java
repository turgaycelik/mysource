package com.atlassian.jira.web.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

/**
 * Groups a collection of authorization checks used by JIRA's view layer.
 *
 * @since v4.3
 * @see com.atlassian.jira.web.action.JiraWebActionSupport
 */
public interface AuthorizationSupport
{
    /**
     * Returns true if the logged in user has the given permission type.
     *
     * @param permName the permission type
     * @return true if the logged in user has the given permission type.
     *
     * @deprecated Use {@link #hasPermission(int)} instead. Since v6.0.
     */
    boolean isHasPermission(String permName);

    /**
     * Returns true if the logged in user has the given permission type.
     *
     * @param permissionsId the permission type
     * @return true if the logged in user has the given permission type.
     *
     * @deprecated Use {@link #hasPermission(int)} instead. Since v6.0.
     */
    boolean isHasPermission(int permissionsId);

    /**
     * Returns true if the logged in user has the given permission type.
     *
     * @param permissionsId the permission type
     * @return true if the logged in user has the given permission type.
     */
    boolean hasPermission(int permissionsId);

    /**
     * Returns true if the logged in user has the given permission type on the given Issue.
     *
     * @param permName the permission type
     * @param issue the Issue
     * @return true if the logged in user has the given permission type on the given Issue.
     *
     * @deprecated Use {@link #hasIssuePermission(int, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean isHasIssuePermission(String permName, GenericValue issue);

    /**
     * Returns true if the logged in user has the given permission type on the given Issue.
     *
     * @param permissionsId the permission type
     * @param issue the Issue
     * @return true if the logged in user has the given permission type on the given Issue.
     *
     * @deprecated Use {@link #hasIssuePermission(int, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean isHasIssuePermission(int permissionsId, GenericValue issue);

    /**
     * Returns true if the logged in user has the given permission type on the given Issue.
     *
     * @param permissionsId the permission type
     * @param issue the Issue
     * @return true if the logged in user has the given permission type on the given Issue.
     */
    boolean hasIssuePermission(int permissionsId, Issue issue);

    /**
     * Returns true if the logged in user has the given permission type on the given Project.
     *
     * @param permName the permission type
     * @param project the Project
     * @return true if the logged in user has the given permission type on the given Project.
     *
     * @deprecated Use {@link #hasProjectPermission(int, com.atlassian.jira.project.Project)} instead. Since v6.0.
     */
    boolean isHasProjectPermission(String permName, GenericValue project);

    /**
     * Returns true if the logged in user has the given permission type on the given Project.
     *
     * @param permissionsId the permission type
     * @param project the Project
     * @return true if the logged in user has the given permission type on the given Project.
     *
     * @deprecated Use {@link #hasProjectPermission(int, com.atlassian.jira.project.Project)} instead. Since v6.0.
     */
    boolean isHasProjectPermission(int permissionsId, GenericValue project);

    /**
     * Returns true if the logged in user has the given permission type on the given Project.
     *
     * @param permissionsId the permission type
     * @param project the Project
     * @return true if the logged in user has the given permission type on the given Project.
     */
    boolean hasProjectPermission(int permissionsId, Project project);

    /**
     * Returns true if remote user has permission over given entity, false otherwise.
     *
     * @param permName permission type
     * @param entity   entity to check the permission for, e.g. project, issue
     *
     * @return true if remote user has permission over given entity, false otherwise
     *
     * @deprecated since 4.3. Please use either {@link #isHasIssuePermission(String, org.ofbiz.core.entity.GenericValue)}, {@link
     *             #isHasIssuePermission(int, org.ofbiz.core.entity.GenericValue)} or {@link #isHasProjectPermission(String, org.ofbiz.core.entity.GenericValue)},
     *             {@link #isHasProjectPermission(int, org.ofbiz.core.entity.GenericValue)}.
     */
    @Deprecated
    boolean isHasPermission(String permName, GenericValue entity);
}
