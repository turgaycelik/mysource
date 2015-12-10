package com.atlassian.jira.rest.v2.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Provide permission information for the current user.
 *
 * @since v5.0
 */
@Path ("mypermissions")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@CorsAllowed
public class PermissionsResource
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;

    public PermissionsResource(PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext, GlobalPermissionManager globalPermissionManager, ProjectManager projectManager, IssueManager issueManager)
    {
        this.globalPermissionManager = globalPermissionManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    /**
     * Returns all permissions in the system and whether the currently logged in user has them. You can optionally provide a specific context to get permissions for
     * (projectKey OR projectId OR issueKey OR issueId)
     * <ul>
     *    <li> When no context supplied the project related permissions will return true if the user has that permission in ANY project </li>
     *    <li> If a project context is provided, project related permissions will return true if the user has the permissions in the specified project.
     *        For permissions that are determined using issue data (e.g Current Assignee), true will be returned if the user meets the permission criteria in ANY issue in that project </li>
     *    <li> If an issue context is provided, it will return whether or not the user has each permission in that specific issue</li>
     * </ul>
     * <p>
     *    NB: The above means that for issue-level permissions (EDIT_ISSUE for example), hasPermission may be true when no context is provided, or when a project context is provided,
     *    <b>but</b> may be false for any given (or all) issues. This would occur (for example) if Reporters were given the EDIT_ISSUE permission. This is because
     *    any user could be a reporter, except in the context of a concrete issue, where the reporter is known.
     * </p>
     * <p>
     *    Global permissions will still be returned for all scopes.
     * </p>
     *
     * @param projectKey - key of project to scope returned permissions for.
     * @param projectId - id of project to scope returned permissions for.
     * @param issueKey - key of the issue to scope returned permissions for.
     * @param issueId - id of the issue to scope returned permissions for.
     *
     * @since v5.0
     *
     * @return all permissions and whether the currently logged in user has them.
     *
     * @response.representation.200.qname
     *      permission
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of all permissions in JIRA and whether the user has them.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.permission.PermissionsJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the project or issue id is invalid.
     *
     * @response.representation.404.doc
     *      Returned if the project or issue id or key is not found.
     */
    @SuppressWarnings ("UnusedDeclaration")
    @GET
    public Response getPermissions(@QueryParam ("projectKey") final String projectKey,
                                    @QueryParam ("projectId") final String projectId,
                                    @QueryParam ("issueKey") final String issueKey,
                                    @QueryParam ("issueId") final String issueId)
    {
        HashMap<String, PermissionJsonBean> permissions = new HashMap<String, PermissionJsonBean>();

        ErrorCollection errors = new ErrorCollection();
        Project project = getProjectByIdOrKey(projectKey, projectId, errors);
        Issue issue = getIssueByIdOrKey(issueKey, issueId, errors);

        if (errors.hasAnyErrors())
        {
            return Response.status(errors.getStatus()).entity(errors).cacheControl(never()).build();
        }

        ApplicationUser loggedInUser = jiraAuthenticationContext.getUser();
        for (Permissions.Permission permission : Permissions.Permission.values())
        {
            boolean hasPermission;
            if (Permissions.Type.GLOBAL != permission.getType())
            {
                if (issue != null)
                {
                    hasPermission = permissionManager.hasPermission(permission.getId(), issue, loggedInUser);
                }
                else if (project != null)
                {
                    hasPermission = permissionManager.hasPermission(permission.getId(), project, loggedInUser);
                }
                else
                {
                    hasPermission = permissionManager.hasProjects(permission.getId(), loggedInUser);
                }
                permissions.put(permission.name(), new PermissionJsonBean(permission, hasPermission, jiraAuthenticationContext));
            }
        }
        for (GlobalPermissionType permission: globalPermissionManager.getAllGlobalPermissions())
        {
            boolean hasPermission = globalPermissionManager.hasPermission(permission.getGlobalPermissionKey(), loggedInUser);
            permissions.put(permission.getKey(), new PermissionJsonBean(permission, hasPermission, jiraAuthenticationContext));
        }

        return Response.ok(new PermissionsJsonBean(permissions)).cacheControl(never()).build();
    }

    private Project getProjectByIdOrKey(String projectKey, String projectId, ErrorCollection errorCollection)
    {
        if (StringUtils.isNotBlank(projectId))
        {
            try
            {
                Project projectObj = projectManager.getProjectObj(Long.parseLong(projectId));
                if (projectObj == null)
                {
                    errorCollection.addErrorMessage("Could not find project with id " + projectId);
                    errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND);
                }
                return projectObj;
            }
            catch(NumberFormatException e)
            {
               errorCollection.addErrorMessage("projectId provided is not valid");
               errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        else if (StringUtils.isNotBlank(projectKey))
        {
            Project project =  projectManager.getProjectObjByKey(projectKey);
            if (project == null)
            {
                errorCollection.addErrorMessage("Could not find project with key " + projectKey);
                errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND);
            }
            return project;
        }
        return null;
    }

    private Issue getIssueByIdOrKey(String issueKey, String issueId, ErrorCollection errorCollection)
    {
        if (StringUtils.isNotBlank(issueId))
        {
            try
            {
                Issue issueObj = issueManager.getIssueObject(Long.parseLong(issueId));
                if (issueObj == null)
                {
                    errorCollection.addErrorMessage("Could not find issue with id " + issueId);
                    errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND);
                }
                return issueObj;
            }
            catch (NumberFormatException e)
            {
                errorCollection.addErrorMessage("issueId provided is not valid");
                errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        else if (StringUtils.isNotBlank(issueKey))
        {
            Issue issueObj = issueManager.getIssueObject(issueKey);
            if (issueObj == null)
            {
                errorCollection.addErrorMessage("Could not find issue with key " + issueKey);
                errorCollection.reason(com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND);
            }
            return issueObj;
        }
        return null;
    }
}
