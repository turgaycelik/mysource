package com.atlassian.jira.web.action.util;

import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.plugin.userformat.UserNameUserFormat;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.util.sharing.SharesListHelper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for displaying a {@link com.atlassian.jira.portal.PortalPage}. Does a lot of the calculations that need to be done.
 *
 * @since v3.13
 */
public class PortalPageDisplayBean implements SharesListHelper
{
    private final JiraAuthenticationContext authCtx;
    private final PortalPage portalPage;
    private final FavouritesService favouriteService;
    private final PermissionManager permissionManager;
    private final ShareTypeFactory shareTypeFactory;
    private final UserFormatManager userFormatManager;

    private Boolean isFavourite = null;
    private Collection<SharePermission> sortedPermissions;
    private Collection<SharePermission> allSharePermissions;

    public PortalPageDisplayBean(final JiraAuthenticationContext authCtx, final PortalPage portalPage, final FavouritesService favouriteService, final PermissionManager permissionManager, final ShareTypeFactory shareTypeFactory, final UserFormatManager userFormatManager)
    {
        this.authCtx = authCtx;
        this.portalPage = portalPage;
        this.favouriteService = favouriteService;
        this.permissionManager = permissionManager;
        this.shareTypeFactory = shareTypeFactory;
        this.userFormatManager = userFormatManager;
    }

    public Long getId()
    {
        return portalPage.getId();
    }

    public String getOwnerUserName()
    {
        final ApplicationUser owner = portalPage.getOwner();
        if (owner != null)
        {
            return userFormatManager.formatUserkey(owner.getKey(), UserNameUserFormat.TYPE, "portal_page");
        }
        return null;
    }

    public String getOwnerFullName()
    {
        final ApplicationUser owner = portalPage.getOwner();
        if (owner != null)
        {
            return userFormatManager.formatUserkey(owner.getKey(), FullNameUserFormat.TYPE, "portal_page");
        }
        return null;
    }

    public String getName()
    {
        return portalPage.getName();
    }

    public boolean isCurrentOwner()
    {
        final ApplicationUser owner = portalPage.getOwner();
        return (owner != null) && owner.equals(authCtx.getUser());
    }

    public String getDescription()
    {
        return portalPage.getDescription();
    }

    /**
     * Is this privately shared?
     *
     * @return true if its has privacy set to Private, else false
     */
    public boolean isPrivate()
    {
        return portalPage.getPermissions().isPrivate();
    }

    /**
     * Is this page a favourite of the current user
     *
     * @return true if the user has favourited this page, else false
     */
    public boolean isFavourite()
    {
        if (isFavourite == null)
        {
            isFavourite = favouriteService.isFavourite(authCtx.getUser(), portalPage);
        }
        return isFavourite;
    }

    /**
     * How many times has this page been favourited
     *
     * @return the count of favourites of this page
     */
    public Long getFavouriteCount()
    {
        return portalPage.getFavouriteCount();
    }

    /**
     * How many times has this page been favourited altered by whether it will disabled or enabled
     *
     * @return the count of favourites of this page altered by whether it will disabled or enabled
     */
    public long getAlternateFavouriteCount()
    {
        return (isFavourite) ? (portalPage.getFavouriteCount().intValue() - 1) : (portalPage.getFavouriteCount().intValue() + 1);
    }

    /**
     * Is it possible to share pages
     *
     * @return true if it is Enterprise or Professional and the current user has the Share permission
     */
    public boolean canShare()
    {
        return permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, authCtx.getLoggedInUser());
    }

    /**
     * Is it possible to edit columns in this version of JIRA
     *
     * @return true if it is possible (it is an Enterprise or Professional instance), else false
     */
    public boolean canEditColumns()
    {
        return true;
    }

    public Collection<SharePermission> getSharePermissions()
    {
        if (sortedPermissions == null)
        {
            final List<SharePermission> permissions;
            if (isCurrentOwner())
            {
                permissions = new ArrayList<SharePermission>(portalPage.getPermissions().getPermissionSet());
            }
            else
            {
                permissions = new ArrayList<SharePermission>();
                for (final SharePermission sharePermission : portalPage.getPermissions())
                {
                    final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                    if (type != null)
                    {
                        final ShareTypePermissionChecker permissionChecker = type.getPermissionsChecker();
                        if (permissionChecker.hasPermission(authCtx.getLoggedInUser(), sharePermission))
                        {
                            permissions.add(sharePermission);
                        }
                    }
                }
            }

            Collections.sort(permissions, shareTypeFactory.getPermissionComparator());
            sortedPermissions = permissions;
        }
        return sortedPermissions;
    }

    //  if the current user has admin rights, then they can see all shares , if not an admin user then you will only
    //  see the shares available to you
    public Collection<SharePermission> getAllSharePermissions()
    {
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser()))
        {
            if (allSharePermissions == null)
            {
                final List<SharePermission> permissions = Lists.newArrayList(portalPage.getPermissions());
                Collections.sort(permissions, shareTypeFactory.getPermissionComparator());
                allSharePermissions = permissions;
            }
            return allSharePermissions;
        }
        else
        {
            return getSharePermissions();
        }
    }

    /**
     * Return HTML view of the current sharing status of the page.
     *
     * @param sharePermission the share permission to render.
     * @return html view of the current sharing state.
     */

    public String getShareView(final SharePermission sharePermission)
    {
        final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
        if (type != null)
        {
            final ShareTypeRenderer shareTypeRenderer = type.getRenderer();
            return shareTypeRenderer.renderPermission(sharePermission, authCtx);
        }
        return null;
    }


    /**
     * Return a simple description of the passed in permission.
     *
     * @param sharePermission The permission to describe.
     * @return a simple description of the passed in permission.
     */
    public String getSimpleDescription(final SharePermission sharePermission)
    {
        final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
        if (type != null)
        {
            final ShareTypeRenderer shareTypeRenderer = type.getRenderer();
            return shareTypeRenderer.getSimpleDescription(sharePermission, authCtx);
        }
        return null;
    }

    public boolean isSystemDashboard()
    {
        return portalPage.isSystemDefaultPortalPage();
    }
}