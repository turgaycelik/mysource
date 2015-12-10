package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories.getBaseURI;

/**
 * @since 4.4
 */
@Path ("customFieldOption")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class CustomFieldOptionResource
{
    private JiraAuthenticationContext authContext;
    private OptionsManager optionsManager;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private I18nHelper i18n;
    private ContextUriInfo contextUriInfo;

    private CustomFieldOptionResource()
    {
        // this constructor used by tooling
    }

    public CustomFieldOptionResource(JiraAuthenticationContext authContext, OptionsManager optionsManager, final VelocityRequestContextFactory velocityRequestContextFactory, I18nHelper i18n, ContextUriInfo contextUriInfo)
    {
        this.authContext = authContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18n = i18n;
        this.contextUriInfo = contextUriInfo;
        this.optionsManager = optionsManager;
    }

    /**
     * Returns a full representation of the Custom Field Option that has the given id.
     *
     * @param id a String containing an Custom Field Option id
     * @return a full representation of the Custom Field Option with the given id
     *
     * @response.representation.200.qname
     *      customFieldOption
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the Custom Field Option exists and is visible by the calling user.
     *
     * @response.representation.200.example
     *       {@link CustomFieldOptionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the Custom Field Option does not exist, or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getCustomFieldOption(@PathParam ("id") final String id)
    {
        Option option = null;
        try
        {
            option = optionsManager.findByOptionId(Long.valueOf(id));
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.custom.field.option.not.found", id)));
        }
        if (option == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.custom.field.option.not.found", id)));
        }

        Response.ResponseBuilder rb = Response.ok(new CustomFieldOptionBeanBuilder()
                .baseURI(getBaseURI(velocityRequestContextFactory))
                .context(contextUriInfo)
                .customFieldOption(option)
                .build()
        );

        return rb.cacheControl(never()).build();
    }
}
