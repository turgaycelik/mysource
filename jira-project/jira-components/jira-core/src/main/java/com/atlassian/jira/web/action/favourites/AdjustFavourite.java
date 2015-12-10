package com.atlassian.jira.web.action.favourites;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.SharedEntityAccessor.Factory;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;

/**
 * Allows the caller to add or remove a favourite. The action can be told to redirect on completion.
 *
 * @since v3.13
 */
public class AdjustFavourite extends JiraWebActionSupport
{
    private final FavouritesService favouritesService;
    private final Factory sharedEntityAccessorFactory;

    private Long entityId = null;
    private String entityType = null;
    private String redirectUrl = null;

    public AdjustFavourite(final FavouritesService favouritesService, final Factory sharedEntityAccessorFactory)
    {
        Assertions.notNull("favouritesService", favouritesService);
        Assertions.notNull("sharedEntityAccessorFactory", sharedEntityAccessorFactory);

        this.sharedEntityAccessorFactory = sharedEntityAccessorFactory;
        this.favouritesService = favouritesService;
    }

    @RequiresXsrfCheck
    public String doAdd() throws Exception
    {
        if (getLoggedInUser() != null)
        {
            if (entityId == null)
            {
                addErrorMessage(getText("favourite.adjust.no.id.specified"));
                return ERROR;
            }
            final JiraServiceContext serviceContext = getJiraServiceContext();
            final SharedEntity sharedEntity = getSharedEntity();
            if (hasAnyErrors() || sharedEntity == null)
            {
                return ERROR;
            }
            favouritesService.addFavourite(serviceContext, sharedEntity);
            if (hasAnyErrors())
            {
                return ERROR;
            }
        }

        return redirect();
    }

    @RequiresXsrfCheck
    public String doRemove() throws Exception
    {
        if (getLoggedInUser() != null)
        {
            if (entityId == null)
            {
                addErrorMessage(getText("favourite.adjust.no.id.specified"));
                return ERROR;
            }

            final JiraServiceContext serviceContext = getJiraServiceContext();
            final SharedEntity sharedEntity = getSharedEntity();
            if (hasAnyErrors() || sharedEntity == null)
            {
                return ERROR;
            }
            favouritesService.removeFavourite(serviceContext, sharedEntity);
            if (hasAnyErrors())
            {
                return ERROR;
            }
        }

        return redirect();
    }

    private SharedEntity getSharedEntity()
    {
        SharedEntity entity = null;
        if (StringUtils.isBlank(entityType))
        {
            addErrorMessage(getText("favourite.adjust.no.type.specified"));
        }
        else
        {
            final SharedEntityAccessor accessor = sharedEntityAccessorFactory.getSharedEntityAccessor(entityType);
            if (accessor == null)
            {
                addErrorMessage(getText("favourite.adjust.invalid.type.specified", entityType));
            }
            else
            {
                entity = accessor.getSharedEntity(getLoggedInUser(), entityId);
                if (entity == null)
                {
                    addErrorMessage(getText("favourite.adjust.no.entity.found"));
                }
            }
        }
        return entity;
    }

    private String redirect()
    {
        if (StringUtils.isEmpty(redirectUrl))
        {
            return getRedirect("MyJiraHome.jspa");
        }
        else
        {
            return getRedirect(redirectUrl);
        }
    }

    public void setEntityId(final Long entityId)
    {
        this.entityId = entityId;
    }

    public void setEntityType(final String entityType)
    {
        this.entityType = entityType;
    }

    public void setRedirectUrl(String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }
}
