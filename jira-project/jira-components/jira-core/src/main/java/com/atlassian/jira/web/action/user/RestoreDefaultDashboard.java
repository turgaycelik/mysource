package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Action to restore the default dashboard.
 *
 * @since ??
 */
public class RestoreDefaultDashboard extends JiraWebActionSupport
{
    private final FavouritesService favouritesService;
    private final EmailFormatter emailFormatter;
    private final GroupPermissionChecker groupPermissionChecker;
    private final PortalPageService portalPageService;

    private boolean confirm;
    private String destination;
    private Map<PortalPage, Long> portalPagesFavouritedByOthersWithUserCount;

    public RestoreDefaultDashboard(PortalPageService portalPageService, FavouritesService favouritesService, EmailFormatter emailFormatter, GroupPermissionChecker groupPermissionChecker)
    {
        this.portalPageService = portalPageService;
        this.favouritesService = favouritesService;
        this.emailFormatter = emailFormatter;
        this.groupPermissionChecker = groupPermissionChecker;
    }

    protected void doValidation()
    {
        if (!confirm)
        {
            addErrorMessage(getText("admin.errors.user.confirm.revert.default"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            final JiraServiceContext serviceContext = getJiraServiceContext();
            final User user = getLoggedInUser();
            Collection<PortalPage> portalPages = portalPageService.getOwnedPortalPages(user);
            for (final PortalPage portalPage : portalPages)
            {
                if (portalPageService.validateForDelete(serviceContext, portalPage.getId()))
                {
                    portalPageService.deletePortalPage(serviceContext, portalPage.getId());
                }
                if (hasAnyErrors())
                {
                    return ERROR;
                }
            }
            final Collection<PortalPage> favouritePortalPages = portalPageService.getFavouritePortalPages(user);
            for (final PortalPage portalPage : favouritePortalPages)
            {
                favouritesService.removeFavourite(serviceContext, portalPage);
                if (hasAnyErrors())
                {
                    return ERROR;
                }
            }
        }
        return getRedirect("Dashboard.jspa");
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getCancelUrl()
    {
        return "ConfigurePortalPages!default.jspa";
    }

    public boolean isHasViewGroupPermission(String group, User user)
    {
        return groupPermissionChecker.hasViewGroupPermission(group, user);
    }

    public String getDisplayEmail(String email)
    {
        return emailFormatter.formatEmailAsLink(email, getLoggedInUser());
    }

    /**
     * Note: result is cached to avoid double calculation (once for checking emptyness, once for iterating).
     *
     * @return a Map containing the current user's {@link com.atlassian.jira.portal.PortalPage}s which have been favourited
     * by other users, and the number of other users who have that PortalPage favourited.
     */
    public Map /*<PortalPage, Long>*/ getPortalPagesFavouritedByOthersWithUserCount()
    {
        if (portalPagesFavouritedByOthersWithUserCount == null)
        {
            Collection<PortalPage> requests = portalPageService.getPortalPagesFavouritedByOthers(getLoggedInApplicationUser());
            portalPagesFavouritedByOthersWithUserCount = new LinkedHashMap<PortalPage, Long>(requests.size());
            for (final PortalPage portalPage : requests)
            {
                long numberFavourites = portalPage.getFavouriteCount().longValue();
                if (favouritesService.isFavourite(getLoggedInApplicationUser(), portalPage))
                {
                    numberFavourites--;
                }
                if (numberFavourites > 0)
                {
                    portalPagesFavouritedByOthersWithUserCount.put(portalPage, Long.valueOf(numberFavourites));
                }
            }
        }
        return portalPagesFavouritedByOthersWithUserCount;
    }
}
