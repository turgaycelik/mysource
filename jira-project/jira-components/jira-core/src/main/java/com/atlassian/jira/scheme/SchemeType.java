package com.atlassian.jira.scheme;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public interface SchemeType
{
    public String getDisplayName();

    public String getType();

    public void doValidation(String key, Map<String, String> parameters, JiraServiceContext jiraServiceContext);

    /**
     * Interface for determining if a permission type has the permission.
     * <p/>
     * This method is called if there is no Remote User (ie anonymous)
     *
     * @param entity   This is the issue or the project that the security is being checked for
     * @param argument If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @return true if anonymous Users have this permission.
     *
     * @deprecated Use {@link #hasPermission(com.atlassian.jira.issue.Issue, String)} or {@link #hasPermission(com.atlassian.jira.project.Project, String)} instead. Since v5.2.
     */
    public boolean hasPermission(GenericValue entity, String argument);

    /**
     * Determines if this permission type is satisfied for anonymous access.
     *
     * @param project   This is the project that the security is being checked for
     * @param parameter If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @return true if anonymous Users have this permission.
     */
    public boolean hasPermission(Project project, String parameter);


    /**
     * Determines if this permission type is satisfied for anonymous access.
     *
     * @param issue   This is the issue that the security is being checked for
     * @param parameter If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @return true if anonymous Users have this permission.
     */
    public boolean hasPermission(Issue issue, String parameter);

    /**
     * Interface for determining if a permission type has the permission
     *
     * @param entity        This is the issue or the project that the security is being checked for
     * @param argument      If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @param user          The user for whom the permission is being checked
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the given User has this permission.
     *
     * @deprecated Use {@link #hasPermission(Project, String, User, boolean)} or {@link #hasPermission(Issue, String, User, boolean)} instead. Since v5.2.
     */
    public boolean hasPermission(GenericValue entity, String argument, User user, boolean issueCreation);

    /**
     * Determines if this permission type is satisfied.
     *
     * @param project       This is the project that the security is being checked for
     * @param parameter      If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @param user          The user for whom the permission is being checked
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the given User has this permission.
     */
    public boolean hasPermission(Project project, String parameter, User user, boolean issueCreation);

    /**
     * Determines if this permission type is satisfied.
     *
     * @param issue       This is the issue that the security is being checked for
     * @param parameter      If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @param user          The user for whom the permission is being checked
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the given User has this permission.
     */
    public boolean hasPermission(Issue issue, String parameter, User user, boolean issueCreation);

    /**
     * This method determines if this SchemeType is valid for the given permissionKey.
     * <p>
     * The default behaviour is for SchemeTypes to be valid for all permission functions, but some scheme types may
     * choose to override this behaviour.
     * eg the CurrentReporterHasCreatePermission scheme is invalid to be added to the "Create Issue" function.
     * Also see JRA-13315.
     * </p>
     *
     * @param permissionKey key of the permission in question
     * @return true if this SchemeType is valid for the given permissionId.
     * @see com.atlassian.jira.security.type.CurrentReporterHasCreatePermission
     */
    public boolean isValidForPermission(ProjectPermissionKey permissionKey);
}
