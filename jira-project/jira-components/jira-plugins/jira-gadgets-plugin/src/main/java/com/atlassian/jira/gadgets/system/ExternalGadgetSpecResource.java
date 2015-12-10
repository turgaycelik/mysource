package com.atlassian.jira.gadgets.system;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Rest resource to remove external gadgets specs
 *
 * @since v4.3
 */
@Path ("externalgadgets")
@Consumes ({ MediaType.APPLICATION_JSON })
public class ExternalGadgetSpecResource
{
    private final ExternalGadgetSpecStore externalGadgetSpecStore;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    public ExternalGadgetSpecResource(final ExternalGadgetSpecStore externalGadgetSpecStore,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager)
    {
        this.externalGadgetSpecStore = externalGadgetSpecStore;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    @DELETE
    @Path ("{gadgetId}")
    public Response deleteExternalGadget(@PathParam ("gadgetId") final String gadgetId)
    {
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser()))
        {
            if (gadgetId != null)
            {
                externalGadgetSpecStore.remove(ExternalGadgetSpecId.valueOf(gadgetId));
                return Response.ok().cacheControl(NO_CACHE).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(NO_CACHE).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).build();
        }
    }
}
