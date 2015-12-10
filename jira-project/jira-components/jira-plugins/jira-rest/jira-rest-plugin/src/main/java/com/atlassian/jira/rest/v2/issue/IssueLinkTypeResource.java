package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.link.IssueLinkTypeService;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.Response.status;

/**
 * Rest resource to retrieve a list of issue link types.
 * @link {com.atlassian.jira.issue.link.IssueLinkType}
 *
 * @since v4.3
 */
@Path ("issueLinkType")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueLinkTypeResource
{
    private final IssueLinkTypeService issueLinkTypeService;
    private final JiraAuthenticationContext authenticationContext;
    private final I18nHelper i18n;
    private ContextUriInfo contextUriInfo;
    private RestUrlBuilder restUrlBuilder;


    public IssueLinkTypeResource(IssueLinkTypeService issueLinkTypeService,
            JiraAuthenticationContext authenticationContext, I18nHelper i18n, ContextUriInfo contextUriInfo, RestUrlBuilder restUrlBuilder)
    {
        this.issueLinkTypeService = issueLinkTypeService;
        this.authenticationContext = authenticationContext;
        this.i18n = i18n;
        this.contextUriInfo = contextUriInfo;
        this.restUrlBuilder = restUrlBuilder;
    }


    /**
     * Returns a list of available issue link types, if issue linking is enabled.
     * Each issue link type has an id, a name and a label for the outward and inward link relationship.
     *
     * @return a list of available issue link types.
     *
     * @response.representation.200.qname
     *      issueLinkTypes
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of all available issue link types.
     *
     * @response.representation.200.example
     *      {@link IssueLinkTypesBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if issue linking is disabled.
     */
    @GET
    public Response getIssueLinkTypes()
    {
        final ServiceOutcome<Collection<IssueLinkType>> outcome = issueLinkTypeService.getIssueLinkTypes(authenticationContext.getLoggedInUser());
        if (outcome.isValid())
        {
            final Collection<IssueLinkType> linkTypes = outcome.getReturnedValue();
            final Iterable<IssueLinkTypeJsonBean> iterable = Iterables.transform(linkTypes, new Function<IssueLinkType, IssueLinkTypeJsonBean>()
            {
                public IssueLinkTypeJsonBean apply(@Nullable IssueLinkType from)
                {
                    final URI uri = restUrlBuilder.getURI(restUrlBuilder.getUrlFor(contextUriInfo.getBaseUri(), IssueLinkTypeResource.class).getIssueLinkType(from.getId().toString()));
                    return new IssueLinkTypeJsonBean(from.getId(), from.getName(), from.getInward(), from.getOutward(), uri);
                }
            });
            return Response.ok(IssueLinkTypesBean.create(Lists.newArrayList(iterable))).cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Create a new issue link type.
     *
     * @request.representation.example
     *      {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE_CREATE}
     *
     * @response.representation.201.doc
     *   The new issue link type has been created.
     *
     * @response.representation.201.qname
     *    issueLinkType
     *
     * @response.representation.201.mediaType
     *    application/json
     *
     * @response.representation.201.example
     *   {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE}
     *
     * @response.representation.404.doc
     *   Issue linking is disabled or you do not have permission to create issue link types.
     */
    @POST
    public Response createIssueLinkType(final IssueLinkTypeJsonBean linkTypeBean)
    {
        validateForCreate(linkTypeBean);
        ServiceOutcome<IssueLinkType> outcome = issueLinkTypeService.createIssueLinkType(authenticationContext.getLoggedInUser(), linkTypeBean.name(), linkTypeBean.outward(), linkTypeBean.inward());
        if (outcome.isValid())
        {
            return status(Response.Status.CREATED).entity(getIssueLinkType(outcome.getReturnedValue().getId().toString()).getEntity()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    private void validateForCreate(final IssueLinkTypeJsonBean linkTypeBean)
    {
        ErrorCollection errors = new ErrorCollection();
        if (StringUtils.isEmpty(linkTypeBean.inward()))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.linking.error.missing.inward"));
        }
        if (StringUtils.isEmpty(linkTypeBean.outward()))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.linking.error.missing.outward"));
        }
        if (StringUtils.isEmpty(linkTypeBean.name()))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.linking.error.missing.name"));
        }

        if (errors.hasAnyErrors())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, errors);
        }
    }


    /**
     * Returns for a given issue link type id all information about this issue link type.
     *
     * @response.representation.200.qname
     *      issueLinkType
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns the issue link type with the given id.
     *
     * @response.representation.200.example
     *      {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if issue linking is disabled or no issue link type with the given id exists.
     *
     * @response.represenation.400.doc
     *     Returned if the supplied id is invalid.
     *
     * @return returns information about an issue link type. Containing the id, name and inward and outward description for
     *         this link.
     */
    @GET
    @Path ("/{issueLinkTypeId}")
    public Response getIssueLinkType(@PathParam ("issueLinkTypeId") final String issueLinkTypeIdString)
    {
        final IssueLinkType linkType = findLinkType(issueLinkTypeIdString);
        final URI uri = restUrlBuilder.getURI(restUrlBuilder.getUrlFor(contextUriInfo.getBaseUri(), IssueLinkTypeResource.class).getIssueLinkType(issueLinkTypeIdString));
        return Response.ok(IssueLinkTypeJsonBean.create(linkType, uri)).cacheControl(never()).build();
    }

    /**
     * Delete the specified issue link type.
     *
     * @response.representation.204.qname
     *      issueLinkType
     *
     * @response.representation.404.doc
     *     Returned if issue linking is disabled or no issue link type with the given id exists.
     *
     * @response.represenation.400.doc
     *     Returned if the supplied id is not a number.
     *
     * @return Returns NO_CONTENT if successful.
     */
    @DELETE
    @Path ("/{issueLinkTypeId}")
    public Response deleteIssueLinkType(@PathParam ("issueLinkTypeId") final String issueLinkTypeIdString)
    {
        final IssueLinkType linkType = findLinkType(issueLinkTypeIdString);
        ServiceOutcome<IssueLinkType> outcome = issueLinkTypeService.deleteIssueLinkType(authenticationContext.getLoggedInUser(), linkType);
        if (outcome.isValid())
        {
            return Response.noContent().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Update the specified issue link type.
     *
     * @request.representation.example
     *      {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE_CREATE}
     *
     * @response.representation.404.doc
     *  Returned if issue linking is disabled or no issue link type with the given id exists.
     *
     * @response.representation.400.doc
     *  Returned if the supplied id is not a number.
     *
     * @response.representation.200.example
     *      {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE}
     */
    @PUT
    @Path ("/{issueLinkTypeId}")
    public Response updateIssueLinkType(@PathParam ("issueLinkTypeId") final String issueLinkTypeIdString, IssueLinkTypeJsonBean linkTypeJsonBean)
    {
        final IssueLinkType linkType = findLinkType(issueLinkTypeIdString);
        final ServiceOutcome<IssueLinkType> outcome = issueLinkTypeService.updateIssueLinkType(authenticationContext.getLoggedInUser(), linkType, linkTypeJsonBean.name(), linkTypeJsonBean.outward(), linkTypeJsonBean.inward());
        if (outcome.isValid())
        {
            return status(Response.Status.OK).entity(getIssueLinkType(issueLinkTypeIdString).getEntity()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Given a link type id as a String, find the actual IssueLinkType.
     * @param issueLinkTypeIdString an id as a String, e.g. "100"
     * @return the matching IssueLinkType
     * @throws RESTException is things goes wrong
     */
    private IssueLinkType findLinkType(String issueLinkTypeIdString)
    {
        final Long issueLinkTypeId;
        try
        {
            issueLinkTypeId = Long.parseLong(issueLinkTypeIdString);
        }
        catch (NumberFormatException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(i18n.getText("rest.issue.link.type.invalid.id", issueLinkTypeIdString)));
        }
        final IssueLinkType linkType;
        try
        {
            ServiceOutcome<Collection<IssueLinkType>> outcome = issueLinkTypeService.getIssueLinkTypes(authenticationContext.getLoggedInUser());
            if (!outcome.isValid())
            {
                throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
            }

            return Iterables.find(outcome.getReturnedValue(), new Predicate<IssueLinkType>()
            {

                public boolean apply(@Nullable IssueLinkType input)
                {
                    return input.getId().equals(issueLinkTypeId);
                }
            });
        }
        catch (NoSuchElementException e)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.issue.link.type.with.id.not.found", issueLinkTypeIdString)));
        }
    }

}
