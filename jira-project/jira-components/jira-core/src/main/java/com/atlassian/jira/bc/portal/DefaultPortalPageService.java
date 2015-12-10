package com.atlassian.jira.bc.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.comparator.PortalPageNameComparator;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The base class for PortalPageService implementations
 *
 * @since v3.13
 */
public class DefaultPortalPageService implements PortalPageService
{
    private final PortalPageManager portalPageManager;
    private final ShareTypeValidatorUtils shareTypeValidatorUtils;
    private final FavouritesManager<PortalPage> favouritesManager;
    private final PermissionManager permissionManager;
    private final UserUtil userUtil;

    public DefaultPortalPageService(final PortalPageManager portalPageManager, final ShareTypeValidatorUtils shareTypeValidatorUtils,
            final FavouritesManager<PortalPage> favouritesManager, final PermissionManager permissionManager, final UserUtil userUtil)
    {
        Assertions.notNull("portalPageManager", portalPageManager);
        Assertions.notNull("shareTypeValidatorUtils", shareTypeValidatorUtils);
        Assertions.notNull("favouritesManager", favouritesManager);
        Assertions.notNull("permissionManager", permissionManager);
        Assertions.notNull("userUtil", userUtil);

        this.userUtil = userUtil;
        this.portalPageManager = portalPageManager;
        this.shareTypeValidatorUtils = shareTypeValidatorUtils;
        this.favouritesManager = favouritesManager;
        this.permissionManager = permissionManager;
    }

    /*
     * ================================== GET / SEARCH METHODS ====================================
     */

    @Override
    public Collection<PortalPage> getFavouritePortalPages(final ApplicationUser user)
    {
        final Collection<Long> ids = getFavouriteIds(user);
        final List<PortalPage> results = new ArrayList<PortalPage>(ids.size());
        for (final Long id : ids)
        {
            final PortalPage portalPage = portalPageManager.getPortalPage(user, id);
            if (portalPage != null)
            {
                results.add(portalPage);
            }
        }
        return results;
    }

    @Override
    public Collection<PortalPage> getFavouritePortalPages(User user)
    {
        return getFavouritePortalPages(ApplicationUsers.from(user));
    }

    @Override
    public boolean isFavourite(final ApplicationUser user, final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);

        if (user == null)
        {
            return false;
        }
        else
        {
            try
            {
                return favouritesManager.isFavourite(user, portalPage);
            }
            catch (PermissionException e)
            {
                return false;
            }
        }
    }

    @Override
    public boolean isFavourite(User user, PortalPage portalPage)
    {
        return isFavourite(ApplicationUsers.from(user), portalPage);
    }

    public Collection<PortalPage> getOwnedPortalPages(final ApplicationUser user)
    {
        if (user == null)
        {
            return Collections.emptyList();
        }
        final Collection<PortalPage> allOwnedPortalPages = portalPageManager.getAllOwnedPortalPages(user);
        return sortByName(allOwnedPortalPages);
    }

    @Override
    public Collection<PortalPage> getOwnedPortalPages(User user)
    {
        return getOwnedPortalPages(ApplicationUsers.from(user));
    }

    public Collection<PortalPage> getNonPrivatePortalPages(final ApplicationUser user)
    {
        final Collection<PortalPage> portalPages = getOwnedPortalPages(user);
        CollectionUtils.filter(portalPages, new Predicate()
        {
            public boolean evaluate(final Object o)
            {
                final PortalPage portalPage = (PortalPage) o;
                return !(portalPage.getPermissions().isPrivate());
            }
        });

        return portalPages;
    }

    @Override
    public Collection<PortalPage> getNonPrivatePortalPages(User user)
    {
        return getNonPrivatePortalPages(ApplicationUsers.from(user));
    }

    public Collection<PortalPage> getPortalPagesFavouritedByOthers(final ApplicationUser user)
    {
        final Collection<PortalPage> nonPrivatePortalPages = getNonPrivatePortalPages(user);
        if (!nonPrivatePortalPages.isEmpty())
        {
            final Collection<Long> favouriteIds = favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE);
            CollectionUtils.filter(nonPrivatePortalPages, new Predicate()
            {
                public boolean evaluate(final Object o)
                {
                    final PortalPage portalPage = (PortalPage) o;

                    // Has someone apart from the owner favourited this portal page?
                    return (favouriteIds.contains(portalPage.getId())) ? portalPage.getFavouriteCount() > 1 : portalPage.getFavouriteCount() > 0;
                }
            });
        }

        return nonPrivatePortalPages;
    }

    @Override
    public Collection<PortalPage> getPortalPagesFavouritedByOthers(User user)
    {
        return getPortalPagesFavouritedByOthers(ApplicationUsers.from(user));
    }

    public PortalPage getPortalPage(final JiraServiceContext context, final Long portalPageId)
    {
        Assertions.notNull("context", context);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = portalPageManager.getPortalPage(context.getLoggedInApplicationUser(), portalPageId);
        if (portalPage == null)
        {
            context.getErrorCollection().addErrorMessage(context.getI18nBean().getText("admin.errors.portalpages.no.access"));
        }

        return portalPage;
    }

    public boolean validateForGetPortalPage(final JiraServiceContext context, final Long portalPageId)
    {
        Assertions.notNull("context", context);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = portalPageManager.getPortalPage(context.getLoggedInApplicationUser(), portalPageId);
        if (portalPage == null)
        {
            context.getErrorCollection().addErrorMessage(context.getI18nBean().getText("admin.errors.portalpages.no.access"));
            return false;
        }

        return true;
    }

    public PortalPage getSystemDefaultPortalPage()
    {
        return portalPageManager.getSystemDefaultPortalPage();
    }

    /*
     * ================================== CRUD METHODS ====================================
     */

    public boolean validateForCreate(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);

        checkPermissionsForCreate(serviceCtx, portalPage);

        if (StringUtils.isBlank(portalPage.getName()))
        {
            serviceCtx.getErrorCollection().addError("portalPageName", serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.specify.name"));
        }
        else if (serviceCtx.getLoggedInApplicationUser() != null)
        {
            final PortalPage byName = portalPageManager.getPortalPageByName(serviceCtx.getLoggedInApplicationUser(), portalPage.getName());
            if (byName != null)
            {
                serviceCtx.getErrorCollection().addError("portalPageName", serviceCtx.getI18nBean().getText("admin.errors.portalpages.same.name"));
            }
        }

        if(StringUtils.isNotBlank(portalPage.getDescription()) && portalPage.getDescription().length() > 255)
        {
            serviceCtx.getErrorCollection().addError("portalPageDescription", serviceCtx.getI18nBean().getText("admin.errors.portalpages.description.too.long"));
        }

        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    public PortalPage createPortalPage(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);

        return createPortalPage(serviceCtx, portalPage, false);
    }

    public PortalPage createPortalPage(final JiraServiceContext serviceCtx, final PortalPage portalPage, final boolean isFavourite)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);

        PortalPage newPortalPage = null;
        if (checkPermissionsForCreate(serviceCtx, portalPage))
        {
            newPortalPage = portalPageManager.create(portalPage);
        }
        if ((newPortalPage != null) && isFavourite)
        {
            try
            {
                favouritesManager.addFavourite(serviceCtx.getLoggedInApplicationUser(), newPortalPage);
            }
            catch (final PermissionException e)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.added"));
            }
        }
        return newPortalPage;
    }

    public boolean validateForCreatePortalPageByClone(final JiraServiceContext serviceCtx, final PortalPage portalPage, final Long clonePortalPageId)
    {
        checkForClone(serviceCtx, portalPage, clonePortalPageId);
        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    private PortalPage checkForClone(final JiraServiceContext serviceCtx, final PortalPage portalPage, final Long clonePortalPageId)
    {
        final boolean ok = validateForCreate(serviceCtx, portalPage);

        PortalPage clonePortalPage = null;
        if (ok && (serviceCtx.getLoggedInApplicationUser() != null))
        {

            final PortalPage tempClonePortalPage = portalPageManager.getPortalPage(serviceCtx.getLoggedInApplicationUser(), clonePortalPageId);
            if (tempClonePortalPage == null)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.clone.does.not.exist"));
            }
            else
            {
                clonePortalPage = tempClonePortalPage;
            }
        }
        return clonePortalPage;
    }

    public PortalPage createPortalPageByClone(final JiraServiceContext serviceCtx, final PortalPage portalPage, final Long clonePortalPageId, final boolean isFavourite)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("clonePortalPageId", clonePortalPageId);

        final PortalPage clonePortalPage = checkForClone(serviceCtx, portalPage, clonePortalPageId);
        if (clonePortalPage == null)
        {
            return null;
        }
        final PortalPage newPortalPage = portalPageManager.createBasedOnClone(serviceCtx.getLoggedInApplicationUser(), portalPage, clonePortalPage);
        if ((newPortalPage != null) && isFavourite)
        {
            try
            {
                favouritesManager.addFavourite(serviceCtx.getLoggedInApplicationUser(), newPortalPage);
            }
            catch (final PermissionException e)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.added"));
            }
        }
        return newPortalPage;
    }

    public boolean validateForDelete(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final ApplicationUser user = serviceCtx.getLoggedInApplicationUser();
        final ErrorCollection errorCollection = serviceCtx.getErrorCollection();
        if (user == null)
        {
            errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.owned.anonymous.user"));
        }
        else
        {
            final PortalPage page = portalPageManager.getPortalPageById(portalPageId);
            if (page == null)
            {
                errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.nonexistent"));
            }
            else if (page.isSystemDefaultPortalPage())
            {
                errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.not.delete.system.default"));
            }
            else if (!user.equals(page.getOwner()))
            {
                errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.be.owner"));
            }
        }
        return !errorCollection.hasAnyErrors();
    }

    public void deleteAllPortalPagesForUser(final ApplicationUser user)
    {
        Assertions.notNull("user", user);

        final Collection<PortalPage> ownedPortalPages = portalPageManager.getAllOwnedPortalPages(user);
        for (final PortalPage portalPage : ownedPortalPages)
        {
            deletePortalPageImpl(portalPage);
        }
        favouritesManager.removeFavouritesForUser(user, PortalPage.ENTITY_TYPE);
    }

    @Override
    public void deleteAllPortalPagesForUser(User user)
    {
        deleteAllPortalPagesForUser(ApplicationUsers.from(user));
    }

    public void deletePortalPage(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final ApplicationUser user = serviceCtx.getLoggedInApplicationUser();

        validateForDelete(serviceCtx, portalPageId);

        if (!serviceCtx.getErrorCollection().hasAnyErrors())
        {
            final PortalPage portalPage = portalPageManager.getPortalPage(user, portalPageId);
            if (portalPage != null)
            {
                deletePortalPageImpl(portalPage);
            }
            else
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.nonexistent"));
            }
        }
    }

    public boolean validateForUpdate(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);

        if (StringUtils.isBlank(portalPage.getName()))
        {
            serviceCtx.getErrorCollection().addError("portalPageName", serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.specify.name"));
        }
        else if (portalPage.isSystemDefaultPortalPage())
        {
            checkSystemDefaultPageForUpdate(serviceCtx, portalPage);
        }
        else
        {
            final boolean ok = checkOwnerShipAndExistenceAndSharePermissions(serviceCtx, portalPage);

            if (ok && (serviceCtx.getLoggedInApplicationUser() != null))
            {
                final PortalPage portalPageByName = portalPageManager.getPortalPageByName(serviceCtx.getLoggedInApplicationUser(), portalPage.getName());
                if ((portalPageByName != null) && !portalPage.getId().equals(portalPageByName.getId()))
                {
                    serviceCtx.getErrorCollection().addError("portalPageName", serviceCtx.getI18nBean().getText("admin.errors.portalpages.same.name"));
                }
            }
        }

        if(StringUtils.isNotBlank(portalPage.getDescription()) && portalPage.getDescription().length() > 255)
        {
            serviceCtx.getErrorCollection().addError("portalPageDescription", serviceCtx.getI18nBean().getText("admin.errors.portalpages.description.too.long"));
        }

        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    public PortalPage updatePortalPage(final JiraServiceContext serviceCtx, final PortalPage portalPage, final boolean isFavourite)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);

        if (validateForUpdate(serviceCtx, portalPage))
        {
            //TODO: Need to do a lot of refactoring here to use the DashboardService for updating most properties
            // except for metadata.  The PortalPage needs to be changed such that it only contains metadata, but no
            // info about portletConfigs.  This will be done once the old dashboard has been removed!
            final PortalPage updatedPortalPage = portalPageManager.update(portalPage);
            if (isFavourite)
            {
                try
                {
                    favouritesManager.addFavourite(serviceCtx.getLoggedInApplicationUser(), updatedPortalPage);
                }
                catch (final PermissionException e)
                {
                    serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.added"));
                }
            }
            else
            {
                favouritesManager.removeFavourite(serviceCtx.getLoggedInApplicationUser(), updatedPortalPage);
            }
            return updatedPortalPage;
        }

        return null;
    }

    public void validateForChangeOwner(final JiraServiceContext serviceCtx, final PortalPage page)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("page", page);
        if (serviceCtx.getLoggedInApplicationUser() != null)
        {
            final PortalPage portalPageByName = portalPageManager.getPortalPageByName(serviceCtx.getLoggedInApplicationUser(), page.getName());
            if ((portalPageByName != null) && !page.getId().equals(portalPageByName.getId()))
            {
                serviceCtx.getErrorCollection().addErrorMessage( serviceCtx.getI18nBean().getText("admin.errors.portalpages.already.owns.same.name", serviceCtx.getLoggedInApplicationUser().getDisplayName()));
            }
            if (page.getPermissions().isPrivate())
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.private"));
            }
        }
    }

    @Override
    public PortalPage updatePortalPageUnconditionally(JiraServiceContext serviceCtx, ApplicationUser user, PortalPage portalPage)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("user", user);
        if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return portalPageManager.update(portalPage);
        }
        else
        {
            return null;
        }
    }

    @Override
    public PortalPage updatePortalPageUnconditionally(JiraServiceContext serviceCtx, User user, PortalPage portalPage)
    {
        return updatePortalPageUnconditionally(serviceCtx, ApplicationUsers.from(user), portalPage);
    }

    /*
     * ================================== SEQUENCE METHODS ====================================
     */
    public boolean validateForChangePortalPageSequence(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        checkPermissionsForMove(serviceCtx, portalPageId);
        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    public void increasePortalPageSequence(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = checkPermissionsForMove(serviceCtx, portalPageId);
        if ((portalPage != null) && !serviceCtx.getErrorCollection().hasAnyErrors())
        {
            try
            {
                favouritesManager.increaseFavouriteSequence(serviceCtx.getLoggedInApplicationUser(), portalPage);
            }
            catch (final PermissionException e)
            {
                // /CLOVER:OFF
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.changed"));
                // /CLOVER:ON
            }
        }
    }

    public void decreasePortalPageSequence(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = checkPermissionsForMove(serviceCtx, portalPageId);
        if ((portalPage != null) && !serviceCtx.getErrorCollection().hasAnyErrors())
        {
            try
            {
                favouritesManager.decreaseFavouriteSequence(serviceCtx.getLoggedInApplicationUser(), portalPage);
            }
            catch (final PermissionException e)
            {
                // /CLOVER:OFF
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.changed"));
                // /CLOVER:ON
            }
        }
    }

    public void moveToStartPortalPageSequence(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = checkPermissionsForMove(serviceCtx, portalPageId);
        if ((portalPage != null) && !serviceCtx.getErrorCollection().hasAnyErrors())
        {
            try
            {
                favouritesManager.moveToStartFavouriteSequence(serviceCtx.getLoggedInApplicationUser(), portalPage);
            }
            catch (final PermissionException e)
            {
                // /CLOVER:OFF
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.changed"));
                // /CLOVER:ON
            }
        }
    }

    public void moveToEndPortalPageSequence(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        final PortalPage portalPage = checkPermissionsForMove(serviceCtx, portalPageId);
        if ((portalPage != null) && !serviceCtx.getErrorCollection().hasAnyErrors())
        {
            try
            {
                favouritesManager.moveToEndFavouriteSequence(serviceCtx.getLoggedInApplicationUser(), portalPage);
            }
            catch (final PermissionException e)
            {
                // /CLOVER:OFF
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.changed"));
                // /CLOVER:ON
            }
        }
    }

    public void saveLegacyPortletConfiguration(final JiraServiceContext serviceCtx, final PortletConfiguration portletConfiguration)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portletConfiguration", portletConfiguration);

        portalPageManager.saveLegacyPortletConfiguration(portletConfiguration);
    }

    public void validateForSearch(final JiraServiceContext serviceCtx, final SharedEntitySearchParameters searchParameters)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("searchParameters", searchParameters);

        final ErrorCollection errorCollection = serviceCtx.getErrorCollection();
        final I18nHelper i18nHelper = serviceCtx.getI18nBean();

        final String searchOwnerUserName = searchParameters.getUserName();
        if (!StringUtils.isBlank(searchOwnerUserName))
        {
            if (!userUtil.userExists(searchOwnerUserName))
            {
                errorCollection.addError("searchOwnerUserName", i18nHelper.getText("admin.errors.portalpages.userdoesnotexist", searchOwnerUserName));
            }
        }
        final ShareTypeSearchParameter shareTypeSearchParameter = searchParameters.getShareTypeParameter();
        if (shareTypeSearchParameter != null)
        {
            shareTypeValidatorUtils.isValidSearchParameter(serviceCtx, shareTypeSearchParameter);
        }
        SharedEntitySearchAction.QueryValidator.validate(searchParameters, errorCollection, serviceCtx.getI18nBean());
    }

    public SharedEntitySearchResult<PortalPage> search(final JiraServiceContext serviceCtx, final SharedEntitySearchParameters searchParameters, final int pagePosition, final int pageWidth)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("searchParameters", searchParameters);

        if (pagePosition < 0)
        {
            throw new IllegalArgumentException("pagePosition < 0");
        }
        if (pageWidth <= 0)
        {
            throw new IllegalArgumentException("pageWidth <= 0");
        }

        return portalPageManager.search(searchParameters, serviceCtx.getLoggedInApplicationUser(), pagePosition, pageWidth);
    }

    public List<List<PortletConfiguration>> getPortletConfigurations(JiraServiceContext serviceCtx, final Long portalPageId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("portalPageId", portalPageId);

        if(validateForGetPortalPage(serviceCtx, portalPageId))
        {
            return portalPageManager.getPortletConfigurations(portalPageId);
        }
        return Collections.emptyList();
    }

    /*
    * ================================== PRIVATE METHODS ====================================
    */

    /**
     * Check that the passed permissions are valid for the System Dashboard.
     *
     * @param serviceCtx       the JiraServiceContext in play
     * @param sharePermissions the Set of {@link com.atlassian.jira.sharing.SharePermission}'s to check
     *
     * @return true if they are valid
     */

    protected boolean checkSystemDefaultSharePermissions(JiraServiceContext serviceCtx, SharePermissions sharePermissions)
    {
        if (!sharePermissions.isGlobal())
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.sysdefault.must.be.public"));
            return false;
        }
        return true;
    }

    /**
     * Check that a user has permission to change the System Default PortalPage.
     * <p/>
     * Package level by design for testing
     *
     * @param serviceCtx the JiraServiceContext in play
     * @param portalPage the PortalPage to check
     *
     * @return true if all is ok. The JiraServiceContext error messages will be filled out if false.
     */
    boolean checkSystemDefaultPageForUpdate(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        if (portalPage.isSystemDefaultPortalPage())
        {
            // then only admin should be able to change the page.
            if (!permissionManager.hasPermission(Permissions.ADMINISTER, serviceCtx.getLoggedInApplicationUser()))
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.be.admin.change.sysdefault"));
                return false;
            }
            else
            {
                // make sure that the dashboard is always publicly available.

                //JRA-15573: We don't need to check the permissions. We want the admin to be
                //able save the page even when they don't have share permission (the page must be shared globally).
                return checkSystemDefaultSharePermissions(serviceCtx, portalPage.getPermissions());
            }
        }
        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    /**
     * Check that the page exists, has the right owner and that the sharing on the page are valid
     *
     * @param serviceCtx the service context in play
     * @param portalPage the portal page in play
     *
     * @return true if its all valid
     */
    boolean checkOwnerShipAndExistenceAndSharePermissions(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        checkOwnerShipAndExistence(serviceCtx, portalPage);
        // now also check that the permission in play for the page are valid according to the share types
        shareTypeValidatorUtils.isValidSharePermission(serviceCtx, portalPage);

        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    /**
     * Check that the page existsand has the right owner.
     *
     * @param serviceCtx the service context in play
     * @param portalPage the portal page in play
     *
     * @return true if its all valid
     */
    boolean checkOwnerShipAndExistence(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        if (serviceCtx.getLoggedInApplicationUser() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.owned.anonymous.user"));
        }
        else if (portalPage.getOwner() == null || !serviceCtx.getLoggedInApplicationUser().equals(portalPage.getOwner()))
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.be.owner"));
        }
        else
        {
            final PortalPage readPage = portalPageManager.getPortalPageById(portalPage.getId());
            if (readPage == null)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.not.saved"));
            }
            else if (!serviceCtx.getLoggedInApplicationUser().equals(readPage.getOwner()))
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.be.owner"));
            }
        }
        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    private PortalPage checkPermissionsForMove(final JiraServiceContext serviceCtx, final Long portalPageId)
    {
        PortalPage portalPage = null;
        if (serviceCtx.getLoggedInApplicationUser() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.owned.anonymous.user"));
        }
        else
        {
            portalPage = getPortalPage(serviceCtx, portalPageId);
            if (portalPage != null)
            {
                if (!isFavourite(serviceCtx.getLoggedInApplicationUser(), portalPage))
                {
                    serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.not.favourite"));
                    portalPage = null;
                }
            }
        }
        return portalPage;
    }


    private boolean checkPermissionsForCreate(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        checkOwnership(serviceCtx, portalPage);
        shareTypeValidatorUtils.isValidSharePermission(serviceCtx, portalPage);

        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    private boolean checkOwnership(final JiraServiceContext serviceCtx, final PortalPage portalPage)
    {
        // check the null user.
        final ApplicationUser user = serviceCtx.getLoggedInApplicationUser();
        final ErrorCollection errorCollection = serviceCtx.getErrorCollection();
        if (user == null)
        {
            errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.owned.anonymous.user"));
        }
        else
        {
            if (portalPage.getOwner() == null || !portalPage.getOwner().equals(user))
            {
                errorCollection.addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.portalpages.must.be.owner"));
            }
        }
        return !errorCollection.hasAnyErrors();
    }

    private static Collection<PortalPage> sortByName(final Collection<PortalPage> portalPages)
    {
        if (portalPages.isEmpty())
        {
            return portalPages;
        }
        else
        {
            final List<PortalPage> portalPagesList = new ArrayList<PortalPage>(portalPages);
            Collections.sort(portalPagesList, PortalPageNameComparator.COMPARATOR);

            return portalPagesList;
        }
    }

    private Collection<Long> getFavouriteIds(final ApplicationUser user)
    {
        return user == null ? Collections.<Long>emptyList() : favouritesManager.getFavouriteIds(user, PortalPage.ENTITY_TYPE);
    }

    private void deletePortalPageImpl(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);

        favouritesManager.removeFavouritesForEntityDelete(portalPage);
        portalPageManager.delete(portalPage.getId());
    }
}
