package com.atlassian.jira.favourites;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultFavouritesManager implements FavouritesManager<SharedEntity>
{
    private static final class Count
    {
        private static final int INCREMENT = 1;
        private static final int DECREMENT = -1;
    }

    private static final CollectionReorderer<SharedEntity> reorderer = new CollectionReorderer<SharedEntity>();

    private final FavouritesStore store;
    private final SharedEntityAccessor.Factory sharedEntityAccessorFactory;
    private final ShareManager shareManager;

    public DefaultFavouritesManager(final FavouritesStore store, final SharedEntityAccessor.Factory sharedEntityAccessorFactory, final ShareManager shareManager)
    {
        this.store = store;
        this.sharedEntityAccessorFactory = sharedEntityAccessorFactory;
        this.shareManager = shareManager;
    }

    @Override
    public void addFavourite(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        validateInput(user, entity);
        checkPermissions(user, entity);

        if (store.addFavourite(user, entity))
        {
            adjustFavouriteCount(entity, Count.INCREMENT);
        }
    }

    @Override
    public void addFavourite(final User user, final SharedEntity entity) throws PermissionException
    {
        addFavourite(ApplicationUsers.from(user), entity);
    }

    public void addFavouriteInPosition(final ApplicationUser user, final SharedEntity entity, final long position) throws PermissionException
    {
        validateInput(user, entity);
        checkPermissions(user, entity);

        if (store.addFavourite(user, entity))
        {
            adjustFavouriteCount(entity, Count.INCREMENT);
            // The fav was added to the end so we will move it to the right position
            reorderFavourites(user, entity, new InsertInPositionReorderCommand((int) position));
        }
    }

    @Override
    public void addFavouriteInPosition(final User user, final SharedEntity entity, final long position) throws PermissionException
    {
        addFavouriteInPosition(ApplicationUsers.from(user), entity, position);
    }

    private void adjustFavouriteCount(final SharedEntity entity, final int incrementCount)
    {
        final SharedEntityAccessor<? extends SharedEntity> countAdjuster = sharedEntityAccessorFactory.getSharedEntityAccessor(entity.getEntityType());
        if (countAdjuster != null)
        {
            countAdjuster.adjustFavouriteCount(entity, incrementCount);
        }
    }

    public void removeFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        validateInput(user, entity);

        if (store.removeFavourite(user, entity))
        {
            adjustFavouriteCount(entity, Count.DECREMENT);
        }
    }

    public void removeFavourite(final User user, final SharedEntity entity)
    {
        removeFavourite(ApplicationUsers.from(user), entity);
    }

    public boolean isFavourite(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        validateInput(user, entity);
        checkPermissions(user, entity);

        return store.isFavourite(user, entity);
    }

    public boolean isFavourite(final User user, final SharedEntity entity) throws PermissionException
    {
        return isFavourite(ApplicationUsers.from(user), entity);
    }

    public Collection<Long> getFavouriteIds(final ApplicationUser user, final SharedEntity.TypeDescriptor<SharedEntity> entityType)
    {
        Assertions.notNull("user", user);
        Assertions.notNull("entity type", entityType);

        //
        // This may return ids of entities that the user cannot see. We have to do this to make it efficient.
        return store.getFavouriteIds(user, entityType);
    }

    public Collection<Long> getFavouriteIds(final User user, final SharedEntity.TypeDescriptor<SharedEntity> entityType)
    {
        return getFavouriteIds(ApplicationUsers.from(user), entityType);
    }

    public void removeFavouritesForUser(final ApplicationUser user, final SharedEntity.TypeDescriptor<SharedEntity> entityType)
    {
        Assertions.notNull("user", user);
        Assertions.notNull("entity type", entityType);

        final Collection<Long> ids = getFavouriteIds(user, entityType);
        for (final Long id : ids)
        {
            // @TODO need to resolve the real objects here, not the Identifier (ClassCastEx anyone?)
            removeFavourite(user, new SharedEntity.Identifier(id, entityType, user));
        }
    }

    public void removeFavouritesForUser(final User user, final SharedEntity.TypeDescriptor<SharedEntity> entityType)
    {
        removeFavouritesForUser(ApplicationUsers.from(user), entityType);
    }

    public void removeFavouritesForEntityDelete(final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity type", entity.getEntityType());
        Assertions.notNull("entity Id", entity.getId());
        store.removeFavouritesForEntity(entity);
        // No need to adjust favourite count as this entity is being deleted
    }

    public void increaseFavouriteSequence(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        reorderFavourites(user, entity, new FavouriteReordererCommand()
        {
            public void reorderFavourites(final List<SharedEntity> favouriteEntities, final SharedEntity entity)
            {
                reorderer.increasePosition(favouriteEntities, entity);
            }
        });
    }

    public void increaseFavouriteSequence(final User user, final SharedEntity entity) throws PermissionException
    {
        increaseFavouriteSequence(ApplicationUsers.from(user), entity);
    }

    public void decreaseFavouriteSequence(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        reorderFavourites(user, entity, new FavouriteReordererCommand()
        {
            public void reorderFavourites(final List<SharedEntity> favouriteEntities, final SharedEntity entity)
            {
                reorderer.decreasePosition(favouriteEntities, entity);
            }
        });
    }

    public void decreaseFavouriteSequence(final User user, final SharedEntity entity) throws PermissionException
    {
        decreaseFavouriteSequence(ApplicationUsers.from(user), entity);
    }

    public void moveToStartFavouriteSequence(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        reorderFavourites(user, entity, new FavouriteReordererCommand()
        {
            public void reorderFavourites(final List<SharedEntity> favouriteEntities, final SharedEntity entity)
            {
                reorderer.moveToStart(favouriteEntities, entity);
            }
        });
    }

    public void moveToStartFavouriteSequence(final User user, final SharedEntity entity) throws PermissionException
    {
        moveToStartFavouriteSequence(ApplicationUsers.from(user), entity);
    }

    public void moveToEndFavouriteSequence(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        reorderFavourites(user, entity, new FavouriteReordererCommand()
        {
            public void reorderFavourites(final List<SharedEntity> favouriteEntities, final SharedEntity entity)
            {
                reorderer.moveToEnd(favouriteEntities, entity);
            }
        });
    }

    public void moveToEndFavouriteSequence(final User user, final SharedEntity entity) throws PermissionException
    {
        moveToEndFavouriteSequence(ApplicationUsers.from(user), entity);
    }

    /**
     * A simple command that will re-order a list of favourite entities in a certain direction. When first class functions arrive, I will die a noble
     * death!
     */
    interface FavouriteReordererCommand
    {
        void reorderFavourites(List<SharedEntity> favouriteEntities, SharedEntity entity);
    }

    static class InsertInPositionReorderCommand implements FavouriteReordererCommand
    {
        final int position;

        InsertInPositionReorderCommand(final int position)
        {
            this.position = position;
        }

        public void reorderFavourites(final List<SharedEntity> favouriteEntities, final SharedEntity entity)
        {
            reorderer.moveToPosition(favouriteEntities, favouriteEntities.size() - 1, position);
        }
    }

    /**
     * Called to re-order the user set of favourites. Also will do some clean up of dead favourite entities and also handles the case where the user
     * no longer has permission to see a favourite (without a delete in this case).
     *
     * @param user the user doing the action
     * @param entity the favourite entity to reorder
     * @param favouriteReordererCommand a simple command say which way to re-order
     */
    void reorderFavourites(final ApplicationUser user, final SharedEntity entity, final FavouriteReordererCommand favouriteReordererCommand)
    {
        validateInput(user, entity);

        final Collection<Long> favIds = store.getFavouriteIds(user, entity.getEntityType());

        final List<SharedEntity> dontHavePermission = new ArrayList<SharedEntity>();
        final List<SharedEntity> favouriteEntities = new ArrayList<SharedEntity>();
        final List<Long> deadFavourites = new ArrayList<Long>();

        SharedEntity targetEntity = null;
        final SharedEntityAccessor<SharedEntity> sharedEntityAccessor = sharedEntityAccessorFactory.getSharedEntityAccessor(entity.getEntityType());
        for (final Long favId : favIds)
        {
            final SharedEntity favEntity = sharedEntityAccessor.getSharedEntity(favId);
            if (favEntity != null)
            {
                if (sharedEntityAccessor.hasPermissionToUse(ApplicationUsers.toDirectoryUser(user), favEntity))
                {
                    favouriteEntities.add(favEntity);
                    if (favEntity.getId().equals(entity.getId()))
                    {
                        targetEntity = favEntity;
                    }
                }
                else
                {
                    dontHavePermission.add(favEntity);
                }
            }
            else
            {
                deadFavourites.add(favId);
            }
        }

        // Don't reorder the entity if it no longer exists.
        if (targetEntity != null)
        {
            favouriteReordererCommand.reorderFavourites(favouriteEntities, targetEntity);
        }

        //
        // now the append the non valid entries to the end.
        favouriteEntities.addAll(dontHavePermission);
        //
        // and take this opportunity to clean up some dead guys!
        removeDeadFavourites(user, deadFavourites, entity.getEntityType());
        //
        // now store the re-ordered list back in the database
        store.updateSequence(user, favouriteEntities);
    }

    /**
     * This is called to clean up favourites that are no longer valid because the entity they point to has been deleted.
     *
     * @param user the user performing the action
     * @param deadFavourites the list of dead favourites ids
     * @param entityType the type of entity being deleted
     */
    private void removeDeadFavourites(final ApplicationUser user, final List<Long> deadFavourites, final SharedEntity.TypeDescriptor<SharedEntity> entityType)
    {
        for (final Object element : deadFavourites)
        {
            final Long favId = (Long) element;

            //
            // The *user* may not be correct here but does not matter. Must put in a user to ensure NPE is not thrown.
            final SharedEntity.Identifier deadEntity = new SharedEntity.Identifier(favId, entityType, user);
            store.removeFavourite(user, deadEntity);
        }
    }

    private boolean hasUserPermissionToSeeEntity(final ApplicationUser user, final SharedEntity entity)
    {
        return shareManager.isSharedWith(user, entity);
    }

    private void validateInput(final ApplicationUser user, final SharedEntity entity)
    {
        Assertions.notNull("user", user);
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity type", entity.getEntityType());
        Assertions.notNull("entity Id", entity.getId());
    }

    private void checkPermissions(final ApplicationUser user, final SharedEntity entity) throws PermissionException
    {
        if (!hasUserPermissionToSeeEntity(user, entity))
        {
            throw new PermissionException(
                "User (" + user + ") does not have permission to see entity - " + entity.getEntityType() + ":" + entity.getId());
        }
    }
}
