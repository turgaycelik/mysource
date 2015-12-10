package com.atlassian.jira.bc.favourites;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

public class DefaultFavouritesService implements FavouritesService
{
    private final FavouritesManager<SharedEntity> favouritesManager;

    public DefaultFavouritesService(final FavouritesManager<SharedEntity> favouritesManager)
    {
        this.favouritesManager = favouritesManager;
    }

    public void addFavourite(final JiraServiceContext ctx, final SharedEntity entity)
    {
        try
        {
            favouritesManager.addFavourite(ctx.getLoggedInApplicationUser(), entity);
        }
        catch (PermissionException e)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("common.favourites.no.permission"));
        }
    }

    public void addFavouriteInPosition(final JiraServiceContext ctx, final SharedEntity entity, final long position)
    {
        try
        {
            favouritesManager.addFavouriteInPosition(ctx.getLoggedInApplicationUser(), entity, position);
        }
        catch (PermissionException e)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("common.favourites.not.added"));
        }
    }

    public void removeFavourite(final JiraServiceContext ctx, final SharedEntity entity)
    {
        favouritesManager.removeFavourite(ctx.getLoggedInApplicationUser(), entity);
    }

    @Override
    public boolean isFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        try
        {
            return !isAnonymous(user) && favouritesManager.isFavourite(user, entity);
        }
        catch (PermissionException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFavourite(final User user, final SharedEntity entity)
    {
        return isFavourite(ApplicationUsers.from(user), entity);
    }

    private boolean isAnonymous(final ApplicationUser user)
    {
        return user == null;
    }
}
