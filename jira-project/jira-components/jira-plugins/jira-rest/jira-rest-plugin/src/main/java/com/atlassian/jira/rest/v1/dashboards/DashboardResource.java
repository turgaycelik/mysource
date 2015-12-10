package com.atlassian.jira.rest.v1.dashboards;

import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.rest.v1.favourites.FavouriteResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST resource to access and modify dashboard information.
 *
 * @since v4.0
 */
@Path ("dashboards")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED  })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
public class DashboardResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final FavouritesService favouritesService;
    private final FavouritesManager favouritesManager;
    private final SharedEntityAccessor.Factory sharedEntityAccessorFactory;

    public DashboardResource(final JiraAuthenticationContext authenticationContext, final FavouritesService favouritesService,
            final FavouritesManager favouritesManager, final SharedEntityAccessor.Factory sharedEntityAccessorFactory)
    {
        Assertions.notNull("authContext", authenticationContext);
        Assertions.notNull("favService", favouritesService);
        Assertions.notNull("favouritesManager", favouritesManager);
        Assertions.notNull("sharedEntityAccessorFactory", sharedEntityAccessorFactory);


        this.authenticationContext = authenticationContext;
        this.favouritesService = favouritesService;
        this.favouritesManager = favouritesManager;
        this.sharedEntityAccessorFactory = sharedEntityAccessorFactory;
    }

    @Path ("{entityId}/favourite")
    public FavouriteResource getFavouriteResource(@PathParam ("entityId") Long entityId)
    {
        return new FavouriteResource(authenticationContext, favouritesService, favouritesManager,
                sharedEntityAccessorFactory, PortalPage.ENTITY_TYPE, entityId);
    }
}
