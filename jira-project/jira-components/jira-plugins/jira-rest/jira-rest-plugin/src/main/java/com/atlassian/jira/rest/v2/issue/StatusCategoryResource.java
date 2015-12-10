package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusCategoryJsonBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.StatusCategoryHelper;
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
 * @since v6.1
 */
@Path("statuscategory")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class StatusCategoryResource
{
    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private StatusCategoryHelper statusCategoryHelper;

    public StatusCategoryResource(JiraAuthenticationContext authContext, ConstantsService constantsService, StatusCategoryHelper statusCategoryHelper)
    {
        this.authContext = authContext;
        this.constantsService = constantsService;
        this.statusCategoryHelper = statusCategoryHelper;
    }

    private StatusCategoryResource()
    {
        // needed for tools that work using reflection
    }

    /**
     * Returns a list of all status categories
     *
     * @since 6.1
     *
     * @param request a Request
     * @param uriInfo a UriInfo
     * @return a full representation of the StatusCategory
     *
     * @response.representation.200.qname
     *      statusCategory
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a list of all JIRA issue status categories in JSON format, that are visible to the user.
     *
     * @response.representation.200.example
     *      {@link StatusCategoryBeanExample#STATUS_CATEGORIES_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if no status categories are found, or the user does not have permission to view them.
     *
     */
    @GET
    public Response getStatusCategories(@Context Request request, @Context UriInfo uriInfo)
    {
        ServiceOutcome<Collection<StatusCategory>> outcome = constantsService.getAllStatusCategories(authContext.getLoggedInUser());

        if (!outcome.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(outcome.getErrorCollection()));
        }

        final List<StatusCategoryJsonBean> beans = new ArrayList<StatusCategoryJsonBean>();
        for (StatusCategory statusCategory : outcome.getReturnedValue())
        {
            final StatusCategoryJsonBean statusCategoryBean = statusCategoryHelper.createStatusCategoryBean(statusCategory, uriInfo, StatusCategoryResource.class);
            beans.add(statusCategoryBean);
        }
        return Response.ok(beans).cacheControl(never()).build();
    }

    /**
     * Returns a full representation of the StatusCategory having the given id or key
     *
     * @param idOrKey a numeric StatusCategory id or a status category key
     * @param request a Request
     * @param uriInfo a UriInfo
     * @return a full representation of the StatusCategory
     *
     * @response.representation.200.qname
     *      statusCategory
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA issue status category in JSON format.
     *
     * @response.representation.200.example
     *      {@link StatusCategoryBeanExample#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue status category is not found, or the user does not have permission to view it.

     */
    @GET
    @Path ("{idOrKey}")
    public Response getStatusCategory(@PathParam("idOrKey") final String idOrKey, @Context Request request, @Context UriInfo uriInfo)
    {
        ServiceOutcome<StatusCategory> statusCategoryOutcome = constantsService.getStatusCategoryById(authContext.getLoggedInUser(), idOrKey);
        if (!statusCategoryOutcome.isValid())
        {
            statusCategoryOutcome = constantsService.getStatusCategoryByKey(authContext.getLoggedInUser(), idOrKey);
            if (!statusCategoryOutcome.isValid())
            {
                throw new NotFoundWebException(ErrorCollection.of(statusCategoryOutcome.getErrorCollection()));
            }
        }

        return Response.ok(statusCategoryHelper.createStatusCategoryBean(statusCategoryOutcome.getReturnedValue(), uriInfo, StatusCategoryResource.class)).cacheControl(never()).build();
    }
}
