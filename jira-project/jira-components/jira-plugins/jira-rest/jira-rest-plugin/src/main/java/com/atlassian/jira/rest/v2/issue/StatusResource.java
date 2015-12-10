package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.util.StatusHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
@Path ("status")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class StatusResource
{
    private ConstantsService constantsService;
    private JiraAuthenticationContext authContext;
	private StatusHelper statusHelper;

    private StatusResource()
    {
        // needed for tools that work using reflection
    }

    public StatusResource(JiraAuthenticationContext authContext, ConstantsService constantsService, StatusHelper statusHelper)
    {
        this.authContext = authContext;
        this.constantsService = constantsService;
		this.statusHelper = statusHelper;
	}

    /**
     * Returns a list of all statuses
     *
     * @since 5.0
     *
     * @param request a Request
     * @param uriInfo a UriInfo
     * @return a full representation of the Status
     *
     *
     * @response.representation.200.qname
     *      statuses
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a list of all JIRA issue statuses in JSON format, that are visible to the user.
     *
     * @response.representation.200.example
     *      {@link StatusBeanExample#STATUSES_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue status is not found, or the user does not have permission to view it.
     *
     */
    @GET
    public Response getStatuses(@Context Request request, @Context UriInfo uriInfo)
    {
        ServiceOutcome<Collection<Status>> outcome = constantsService.getAllStatuses(authContext.getLoggedInUser());
        if (!outcome.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(outcome.getErrorCollection()));
        }

        final List<StatusJsonBean> beans = new ArrayList<StatusJsonBean>();
        for (Status status : outcome.getReturnedValue())
        {
            final StatusJsonBean statusBean = statusHelper.createStatusBean(status, uriInfo, StatusResource.class);
            beans.add(statusBean);
        }
        return Response.ok(beans).cacheControl(never()).build();
    }

    /**
     * Returns a full representation of the Status having the given id or name.
     *
     * @param idOrName a numeric Status id or a status name
     * @param request a Request
     * @param uriInfo a UriInfo
     * @return a full representation of the Status
     *
     * @response.representation.200.qname
     *      status
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA issue status in JSON format.
     *
     * @response.representation.200.example
     *      {@link StatusBeanExample#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue status is not found, or the user does not have permission to view it.

     */
    @GET
    @Path ("{idOrName}")
    public Response getStatus(@PathParam ("idOrName") final String idOrName, @Context Request request, @Context UriInfo uriInfo)
    {
        ServiceOutcome<Status> statusOutcome = constantsService.getStatusById(authContext.getLoggedInUser(), idOrName);
        if (!statusOutcome.isValid())
        {
            statusOutcome = constantsService.getStatusByTranslatedName(authContext.getLoggedInUser(), idOrName);
            if (!statusOutcome.isValid())
            {
                throw new NotFoundWebException(ErrorCollection.of(statusOutcome.getErrorCollection()));
            }
        }

        return Response.ok(statusHelper.createStatusBean(statusOutcome.getReturnedValue(), uriInfo, StatusResource.class)).cacheControl(never()).build();
    }
}
