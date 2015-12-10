package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
@Path ("issuetype")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueTypeResource
{
    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private ContextUriInfo contextUriInfo;
    private JiraBaseUrls jiraBaseUrls;

    private IssueTypeResource()
    {
        // this constructor used by tooling
    }

    public IssueTypeResource(JiraAuthenticationContext authContext, final ConstantsService constantsService, final VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo, JiraBaseUrls jiraBaseUrls)
    {
        this.authContext = authContext;
        this.constantsService = constantsService;
        this.contextUriInfo = contextUriInfo;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    /**
     * Returns a list of all issue types visible to the user
     *
     * @return a list of issue types
     *
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue type exists and is visible by the calling user.
     *
     * @response.representation.200.example
     *      {@link IssueTypeBeanExample#ISSUE_TYPES_EXAMPLE}
     */
    @GET
    public Response getIssueAllTypes()
    {
        ServiceOutcome<Collection<IssueType>> outcome = constantsService.getAllIssueTypes(authContext.getLoggedInUser());
        if (!outcome.isValid())
        {
            throw new ServerErrorWebException(ErrorCollection.of(outcome.getErrorCollection()));
        }

        final List<IssueTypeJsonBean> beans = new ArrayList<IssueTypeJsonBean>();
        for (IssueType issueType : outcome.getReturnedValue())
        {
            final IssueTypeJsonBean issueTypeBean = createIssueTypeBean(issueType);
            beans.add(issueTypeBean);
        }
        return Response.ok(beans).cacheControl(never()).build();
    }

    /**
     * Returns a full representation of the issue type that has the given id.
     *
     * @param issueTypeId a String containing an issue type id
     * @return a full representation of the issue type with the given id
     *
     * @response.representation.200.qname
     *      issueType
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue type exists and is visible by the calling user.
     * @response.representation.200.example
     *      {@link IssueTypeBeanExample#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue type does not exist, or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getIssueType(@PathParam ("id") final String issueTypeId)
    {
        ServiceOutcome<IssueType> outcome = constantsService.getIssueTypeById(authContext.getLoggedInUser(), issueTypeId);
        if (!outcome.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(outcome.getErrorCollection()));
        }

        IssueType returnedValue = outcome.getReturnedValue();
        IssueTypeJsonBean bean = createIssueTypeBean(returnedValue);

        return Response.ok(bean).cacheControl(never()).build();
    }

    private IssueTypeJsonBean createIssueTypeBean(IssueType returnedValue)
    {
        return new IssueTypeBeanBuilder()
                .jiraBaseUrls(jiraBaseUrls)
                .context(contextUriInfo)
                .issueType(returnedValue)
                .build();
    }
}
