package com.atlassian.jira.rest.internal;

import com.atlassian.classloader.TomcatResourceEntriesCacheFlusher;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Enables authorised users to manipulate and configure cache behaviour in JIRA.
 *
 * @since v6.1
 */
@Path ("cache")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class CacheResource
{
    private final EventPublisher eventPublisher;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;

    public CacheResource(EventPublisher eventPublisher, JiraAuthenticationContext jiraAuthenticationContext, PermissionManager permissionManager)
    {
        this.eventPublisher = eventPublisher;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
    }

    /**
     * Invokes the system-wide clear cache event which should cause all caches which have subscribed to this to clear
     * their contents completely. Typical configuration ensures that it will happen asynchronously although that is not
     * guaranteed. Note that requests to the application after calling this may take an initial performance hit until
     * the relevant caches are warmed up again.
     * <p>
     * The intended purpose of this is to shrink the memory usage of a JIRA instance which is not being actively used.
     */
    @Consumes (MediaType.WILDCARD)
    @DELETE
    @Path("app")
    public Response clearCaches()
    {
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraAuthenticationContext.getUser()))
        {
            throw new NotAuthorisedWebException();
        }
        eventPublisher.publish(ClearCacheEvent.INSTANCE);
        return Response.ok().cacheControl(never()).build();
    }

    /**
     * Invokes the system-wide clear cache event which should cause all caches which have subscribed to this to clear
     * their contents completely. Typical configuration ensures that it will happen asynchronously although that is not
     * guaranteed. Note that requests to the application after calling this may take an initial performance hit until
     * the relevant caches are warmed up again.
     * <p>
     * The intended purpose of this is to shrink the memory usage of a JIRA instance which is not being actively used.
     */
    @Consumes (MediaType.WILDCARD)
    @DELETE
    @Path("classloader")
    public Response clearClassLoaderCache()
    {
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraAuthenticationContext.getUser()))
        {
            throw new NotAuthorisedWebException();
        }
        new TomcatResourceEntriesCacheFlusher().run();
        return Response.ok().cacheControl(never()).build();
    }


}
