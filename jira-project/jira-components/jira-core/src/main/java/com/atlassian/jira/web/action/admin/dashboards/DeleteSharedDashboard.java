package com.atlassian.jira.web.action.admin.dashboards;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * The Delete Shared Dashboards action
 *
 * @since v4.4
 */
public class DeleteSharedDashboard extends AbstractDashboardAdministration
{
    private final UserPickerSearchService userPickerSearchService;
    private final AvatarService avatarService;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final PortalPageService portalPageService;
    private final ShareTypeValidatorUtils shareTypeValidatorUtils;
    private final FavouritesService favouriteService;


    private Long otherFavouriteCount;
    private Collection subscriptions;
    private static final int DASHBOARDS_PER_PAGE = 20;


    public DeleteSharedDashboard(final PermissionManager permissionManager, final UserPickerSearchService userPickerSearchService,
            final AvatarService avatarService, final UserManager userManager,  final FavouritesService favouriteService,
            final PortalPageService portalPageService, final PortalPageManager portalPageManager, final ShareTypeValidatorUtils shareTypeValidatorUtils)
    {
        super(permissionManager, portalPageManager);
        this.permissionManager = permissionManager;
        this.userPickerSearchService = userPickerSearchService;
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.portalPageService = portalPageService;
        this.shareTypeValidatorUtils = shareTypeValidatorUtils;
        this.favouriteService = favouriteService;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getDashboardId() != null)
        {
            final JiraServiceContext ctx = getJiraServiceContext(getDashboardId());
            portalPageService.validateForDelete(ctx, getDashboardId());

            if (hasAnyErrors())
            {
                return ERROR;
            }
            portalPageService.deletePortalPage(getJiraServiceContext(getDashboardId()), getDashboardId());
            if (hasAnyErrors())
            {
                return ERROR;
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.dashboards.cannot.delete.dashboard"));
            return ERROR;
        }
        repaginateIfNeeded();
        setDashboard(null);
        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect(buildReturnUri());
        }
        else
        {
            String returnUrl =  buildReturnUri();
            setReturnUrl(null);
            return forceRedirect(returnUrl);
        }
    }

    private void repaginateIfNeeded()
    {
        // only need to repaginate if on last page
        final int pagingOffset = StringUtils.isNotBlank(getPagingOffset()) ? Integer.parseInt(getPagingOffset()) - 1 : -1;
        final int newResultCount = StringUtils.isNotBlank(getTotalResultCount()) ? Integer.parseInt(getTotalResultCount()) - 1 :  -1;
        if (pagingOffset >= 0)
        {
            setTotalResultCount(""+newResultCount);
            if (newResultCount % DASHBOARDS_PER_PAGE == 0)
            {
                setPagingOffset(""+pagingOffset);
            }
        }
    }

    public int getOtherFavouriteCount()
    {
        if (otherFavouriteCount == null)
        {
            final PortalPage dashboard = getDashboard();

            // We want to know how many times it has been favourited by OTHER people
            ApplicationUser dashboardOwner = dashboard.getOwner();
            final boolean isFavourite = favouriteService.isFavourite(dashboardOwner, dashboard);
            final int count = isFavourite ? dashboard.getFavouriteCount().intValue() - 1 : dashboard.getFavouriteCount().intValue();
            otherFavouriteCount = (long) count;
        }
        return otherFavouriteCount.intValue();
    }


    public boolean canDelete()
    {
        return !hasAnyErrors();
    }

}
