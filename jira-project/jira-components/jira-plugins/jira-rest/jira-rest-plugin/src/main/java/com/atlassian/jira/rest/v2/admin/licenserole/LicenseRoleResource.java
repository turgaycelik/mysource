package com.atlassian.jira.rest.v2.admin.licenserole;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.license.LicenseRoleService;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.api.util.ErrorCollection.of;

/**
 * Provides REST access to JIRA's License Roles.
 *
 * @since v6.3
 */
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@WebSudoRequired
@Path ("licenserole")
public class LicenseRoleResource
{
    private final LicenseRoleService service;
    private final I18nHelper helper;

    public LicenseRoleResource(final LicenseRoleService service, final I18nHelper helper)
    {
        this.helper = helper;
        this.service = Assertions.notNull("service", service);
    }

    /**
     * Returns all license roles in the system.
     *
     * @return all license roles in the system.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned when the caller has permission to see the license roles.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleBeanExamples#DOC_LIST}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     *
     * @response.representation.403.doc
     *      Returned if the user does not have permission to see license roles.
     */
    @GET
    public Response getAll()
    {
        return responseForIterableOutcome(service.getRoles(), LicenseRoleBean.TO_BEAN);
    }

    /**
     * Returns the passed license role if it exists.
     *
     * @param roleId the id of the role to use.
     *
     * @return the passed license role if it exists.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the caller has permission to the license role.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleBeanExamples#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     * @response.representation.404.doc
     *      Returned if the passed license role does not exist.
     * @response.representation.403.doc
     *      Returned if the user does not have permission to see the license role.
     */
    @Path("{roleId}")
    @GET
    public Response get(@PathParam("roleId") final String roleId)
    {
        return withLicenseRole(roleId, new Function<LicenseRoleId, Response>()
        {
            @Override
            public Response apply(final LicenseRoleId input)
            {
                return responseFromOutcome(service.getRole(input), LicenseRoleBean.TO_BEAN);
            }
        });
    }

    /**
     * Updates the license role with the passed data. Only the groups of the role may be updated.
     * Requests to change the id or the name of the role will be silently ignored.
     *
     * @param roleId the id of the role to use.
     * @param bean the data to update the role with.
     *
     * @return the updated license role if the update was successful.
     *
     * @request.representation.mediaType
     *      application/json
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleBeanExamples#UPDATE_EXAMPLE}
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the caller has permission to update license role.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleBeanExamples#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     *
     * @response.representation.404.doc
     *      Returned if the passed license role does not exist.
     *
     * @response.representation.403.doc
     *      Returned if the user does not have permission to update the license role.
     */
    @Path("{roleId}")
    @PUT
    public Response put(@PathParam("roleId") final String roleId, final LicenseRoleBean bean)
    {
        if (bean == null || bean.getGroups() == null)
        {
            return get(roleId);
        }
        else
        {
            return withLicenseRole(roleId, new Function<LicenseRoleId, Response>()
            {
                @Override
                public Response apply(final LicenseRoleId input)
                {
                    return responseFromOutcome(service.setGroups(input, bean.getGroups()), LicenseRoleBean.TO_BEAN);
                }
            });
        }
    }

    private Response withLicenseRole(String id, Function<LicenseRoleId, Response> valid)
    {
        if (StringUtils.isBlank(id))
        {
            return forError(helper.getText("rest.error.no.license.role", id));
        }
        else
        {
            return valid.apply(LicenseRoleId.valueOf(id));
        }
    }

    private static Response ok(final Object bean)
    {
        return build(Response.ok(bean));
    }

    private static <I, O> Response responseFromOutcome(ServiceOutcome<I> outcome,
            final Function<? super I, ? extends O> transform)
    {
        if (outcome.isValid())
        {
            return ok(transform.apply(outcome.get()));
        }
        else
        {
            return forCollection(outcome.getErrorCollection());
        }
    }

    private static <I, O> Response responseForIterableOutcome(final ServiceOutcome<? extends Iterable<I>> outcome,
            final Function<? super I, ? extends O> transform)
    {
        return responseFromOutcome(outcome, new Function<Iterable<I>, Object>()
        {
            @Override
            public Object apply(Iterable<I> input)
            {
                return Lists.newArrayList(Iterables.transform(input, transform));
            }
        });
    }

    private static Response forError(String message)
    {
        return build(Response.status(Response.Status.BAD_REQUEST).entity(of(message)));
    }

    private static Response forCollection(ErrorCollection collection)
    {
        return forCollection(collection, ErrorCollection.Reason.SERVER_ERROR);
    }

    private static Response forCollection(ErrorCollection collection, ErrorCollection.Reason defaultReason)
    {
        if (!collection.hasAnyErrors())
        {
            throw new IllegalArgumentException("collection has no errors.");
        }
        if (defaultReason == null)
        {
            throw new IllegalArgumentException("defaultReason is null");
        }

        ErrorCollection.Reason worstReason = ErrorCollection.Reason.getWorstReason(collection.getReasons());
        if (worstReason == null)
        {
            worstReason = defaultReason;
        }
        return build(Response.status(worstReason.getHttpStatusCode()).entity(of(collection)));
    }

    private static Response build(Response.ResponseBuilder builder)
    {
        return builder.cacheControl(never()).build();
    }

}
