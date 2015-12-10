package com.atlassian.jira.rest.v1.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Dodgy project avatar REST resource needed to update the avatar for a particular project.
 *
 * @since v4.4
 */
@Path ("project")
@Produces ( { MediaType.APPLICATION_JSON })
@CorsAllowed
public class ProjectAvatarResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;
    private AvatarManager avatarManager;
    private final XsrfInvocationChecker xsrfChecker;

    public ProjectAvatarResource(JiraAuthenticationContext authenticationContext, final ProjectService projectService,
            final AvatarManager avatarManager, final XsrfInvocationChecker xsrfChecker)
    {
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
        this.avatarManager = avatarManager;
        this.xsrfChecker = xsrfChecker;
    }

    @POST
    @Path ("{projectKey}/avatar/{avatarId}")
    public Response updateUserAvatar(@PathParam ("projectKey") String projectKey, @PathParam ("avatarId") Long avatarId)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        if (StringUtils.isBlank(projectKey) || avatarId == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project key and avatarId are required parameters").cacheControl(NO_CACHE).build();
        }

        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKey(authenticationContext.getLoggedInUser(), projectKey);
        if (!projectResult.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(projectResult.getErrorCollection())).cacheControl(NO_CACHE).build();
        }
        final Avatar avatar = avatarManager.getById(avatarId);
        if(avatar == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }

        final Project project = projectResult.getProject();
        final ProjectService.UpdateProjectValidationResult updateValidationResult = projectService.validateUpdateProject(authenticationContext.getLoggedInUser(), project.getName(), project.getKey(),
                project.getDescription(), project.getLeadUserName(), project.getUrl(), project.getAssigneeType(), avatarId);
        if (!updateValidationResult.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(updateValidationResult.getErrorCollection())).cacheControl(NO_CACHE).build();
        }
        projectService.updateProject(updateValidationResult);
        return Response.ok().cacheControl(NO_CACHE).build();
    }
}
