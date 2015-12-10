package com.atlassian.jira.rest.v1.users;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarsDisabledException;
import com.atlassian.jira.avatar.NoPermissionException;
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
 * User REST resource.
 *
 * @since v4.2
 */
@Path ("user")
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
public class UserResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final AvatarService avatarService;
    private final XsrfInvocationChecker xsrfChecker;

    public UserResource(JiraAuthenticationContext authenticationContext, AvatarService avatarService, XsrfInvocationChecker xsrfChecker)
    {
        this.authenticationContext = authenticationContext;
        this.avatarService = avatarService;
        this.xsrfChecker = xsrfChecker;
    }

    @POST
    @Path ("{username}/avatar/{avatarid}")
    public Response updateUserAvatar(@PathParam ("username") String username, @PathParam ("avatarid") Long avatarId)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        if (StringUtils.isBlank(username) || avatarId == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("username and avatarid are required path parameters!").cacheControl(NO_CACHE).build();
        }

        try
        {
            avatarService.setCustomUserAvatar(authenticationContext.getLoggedInUser(), username, avatarId);

            return Response.ok().cacheControl(NO_CACHE).build();
        }
        catch (AvatarsDisabledException e)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        catch (NoPermissionException e)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
    }
}
