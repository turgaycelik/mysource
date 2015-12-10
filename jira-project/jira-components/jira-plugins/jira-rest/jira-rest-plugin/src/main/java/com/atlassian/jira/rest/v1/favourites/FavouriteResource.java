package com.atlassian.jira.rest.v1.favourites;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.BooleanResult;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.v1.model.errors.ErrorCollection.Builder;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Allows of favouriting and unfavouriting of shared entities.  Actions can also be undone.
 *
 * @since v4.0
 */
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
//TODO: Probably needs fixing for RenameUser as well
public class FavouriteResource
{
    private static final Logger log = Logger.getLogger(FavouriteResource.class);

    private final JiraAuthenticationContext authContext;
    private final FavouritesService favService;
    private final FavouritesManager favouritesManager;
    private final SharedEntityAccessor.Factory sharedEntityAccessorFactory;
    private final SharedEntity.TypeDescriptor entityType;
    private final Long entityId;

    /**
     * Main constructor for FavouritesResource.
     *
     * @param authContext the JIRA authentication context
     * @param favService the FavouritesService that is delegated
     * @param favouritesManager the fav manager
     * @param sharedEntityAccessorFactory factory used to create SharedEntityAccessor objects when looking up shared
     * entities.
     * @param entityType the entity type to favourite - SearchRequest, PortalPage
     * @param entityId id of the entity to favourite
     */
    public FavouriteResource(final JiraAuthenticationContext authContext, final FavouritesService favService, final FavouritesManager favouritesManager,
            final SharedEntityAccessor.Factory sharedEntityAccessorFactory, final SharedEntity.TypeDescriptor entityType,
            final Long entityId)
    {
        Assertions.notNull("authContext", authContext);
        Assertions.notNull("favService", favService);
        Assertions.notNull("favouritesManager", favouritesManager);
        Assertions.notNull("sharedEntityAccessorFactory", sharedEntityAccessorFactory);
        Assertions.notNull("entityType", entityType);
        Assertions.notNull("entityId", entityId);

        this.authContext = authContext;
        this.favService = favService;
        this.favouritesManager = favouritesManager;
        this.sharedEntityAccessorFactory = sharedEntityAccessorFactory;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    @GET
    /**
     * Returns if a particular entity is favourited by a user
     *
     * @return true or false, 404 if entity not found or there's a permission exception.
     */
    public Response isFavourite()
    {
        try
        {
            final SharedEntity entity = getSharedEntity();
            return Response.ok(new BooleanResult(favService.isFavourite(authContext.getUser(), entity))).cacheControl(NO_CACHE).build();
        }
        catch (RuntimeException e)
        {
            //thrown on permission exception.  return a 404 in this case
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
    }


    /**
     * Adds an entity to a user's list of favourites
     *
     * @return 200 if successful
     */
    @PUT
    public Response addFavourite()
    {
        return setFavourite(entityType, entityId, true, null);
    }

    /**
     * Removes an entity to a user's list of favourites
     *
     * @return 200 if successful
     */
    @DELETE
    public Response deleteFavourite()
    {
        return setFavourite(entityType, entityId, false, null);
    }

    /**
     * Undo an operation, and re-insert a favourite at a certain position.
     *
     * @param originalOrder - the original order that entries where in
     * @return 200 on success
     */
    @POST
    public Response undo(final OriginalOrder originalOrder)
    {
        return setFavourite(entityType, entityId, true, originalOrder != null ? originalOrder.getEntries() : null);
    }

    private Response setFavourite(final SharedEntity.TypeDescriptor entityType, final Long entityId, final boolean enable, final List<Long> originalOrder)
    {
        final JiraServiceContext ctx = getContext();

        if (log.isDebugEnabled())
        {
            log.debug("Set favourite for : " + ctx.getLoggedInUser() + " (" + entityType + " - " + entityId + ") - " + enable);
        }
        final SharedEntity entity = getSharedEntity();
        if (enable)
        {
            if (originalOrder != null)
            {
                final long positionForFavourite = getPositionForFavourite(ctx.getLoggedInApplicationUser(), entity, originalOrder);
                favService.addFavouriteInPosition(ctx, entity, positionForFavourite);
            }
            else
            {
                favService.addFavourite(ctx, entity);
            }
        }
        else
        {
            favService.removeFavourite(ctx, entity);
        }

        final boolean success = !ctx.getErrorCollection().hasAnyErrors();
        if (!success)
        {
            log.debug("Unable to set favourite for : " + ctx.getLoggedInUser() + " (" + entityType + " - " + entityId + ") - " + enable + ":\n" + ctx.getErrorCollection());
            return Response.status(400).entity(Builder.newBuilder().addErrorCollection(ctx.getErrorCollection()).build()).
                    cacheControl(NO_CACHE).build();
        }
        else if (log.isDebugEnabled())
        {
            log.debug("Success in setting favourite for : " + ctx.getLoggedInUser() + " (" + entityType + " - " + entityId + ") - " + enable);
        }
        return Response.ok(new TextMessage("Success in setting favourite for : " + ctx.getLoggedInUser() + " (" + entityType + " - " + entityId + ") - " + "favourite state: " + enable)).cacheControl(NO_CACHE).build();
    }

    long getPositionForFavourite(final ApplicationUser user, final SharedEntity entityToUndo, final List<Long> orginalOrder)
    {
        final List<Long> newOrderedEntities = new ArrayList<Long>();
        // Get the current id's
        @SuppressWarnings ("unchecked")
        final Collection<Long> existingIds = favouritesManager.getFavouriteIds(user, entityToUndo.getEntityType());
        final Long entityToUndoId = entityToUndo.getId();
        for (Long entityId : orginalOrder)
        {
            if (existingIds.contains(entityId))
            {
                newOrderedEntities.add(entityId);
            }
            else if (entityId.equals(entityToUndoId))
            {
                return newOrderedEntities.size();
            }
        }
        return -1;
    }

    private SharedEntity getSharedEntity()
    {
        @SuppressWarnings ("unchecked")
        final SharedEntityAccessor accessor = sharedEntityAccessorFactory.getSharedEntityAccessor(entityType);
        if (accessor == null)
        {
            throw new IllegalArgumentException("Unsupported entityType");
        }
        else
        {
            final SharedEntity entity = accessor.getSharedEntity(authContext.getLoggedInUser(), entityId);
            if (entity == null)
            {
                throw new IllegalArgumentException(getText("common.favourites.entity.not.available"));
            }
            return entity;
        }
    }

    private JiraServiceContext getContext()
    {
        return new JiraServiceContextImpl(authContext.getLoggedInUser());
    }

    /**
     * Translates a given key using i18n bean
     *
     * @param key key to translate
     * @return i18n string for given key
     */
    private String getText(final String key)
    {
        return authContext.getI18nHelper().getText(key);
    }

    @XmlRootElement
    public static class OriginalOrder
    {
        @XmlElement
        private List<Long> entries = new ArrayList<Long>();

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private OriginalOrder() {}

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        public OriginalOrder(final List<Long> entries)
        {
            this.entries = entries;
        }

        public List<Long> getEntries()
        {
            return entries;
        }
    }
}
