package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.issue.subscription.FilterSubscription;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("filterSubscription")
public class FilterSubscriptionBackdoor
{
    private final FilterSubscriptionService filterSubscriptionService;
    private final SubscriptionManager subscriptionManager;
    private final JiraAuthenticationContext authContext;
    private JiraServiceContext serviceContext;

    public FilterSubscriptionBackdoor(FilterSubscriptionService filterSubscriptionService, final SubscriptionManager subscriptionManager, JiraAuthenticationContext authContext)
    {
        this.filterSubscriptionService = filterSubscriptionService;
        this.subscriptionManager = subscriptionManager;
        this.authContext = authContext;
    }

    @POST
    public Response addSubscription(@QueryParam ("filterId") Long filterId,
            @QueryParam ("groupName") String groupName, @QueryParam ("expr") String expr, @QueryParam ("emailOnEmpty") boolean emailOnEmpty)
    {
        serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser(), new SimpleErrorCollection());
        filterSubscriptionService.storeSubscription(serviceContext, filterId, groupName, expr, emailOnEmpty);
        return Response.ok(null).build();
    }

    @GET
    @Path("{id}")
    public Response getSubscription(@PathParam ("id") Long id)
    {
        serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser(), new SimpleErrorCollection());
        try
        {
            GenericValue subscription = subscriptionManager.getSubscription(authContext.getUser(), id);
            return Response.ok().entity(subscription).build();
        }
        catch (GenericEntityException e)
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{id}/cron")
    public Response getCronForSubscription(@PathParam ("id") Long id)
    {
        serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser(), new SimpleErrorCollection());
        try
        {
            FilterSubscription subscription = subscriptionManager.getFilterSubscription(authContext.getUser(), id);
            if (subscription == null)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            String cron = subscriptionManager.getCronExpressionForSubscription(subscription);
            return Response.ok().entity(cron).build();
        }
        catch (GenericEntityException e)
        {
            return Response.serverError().build();
        }
    }

}
