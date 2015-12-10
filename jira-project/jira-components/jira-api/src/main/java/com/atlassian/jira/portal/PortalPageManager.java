package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.util.Collection;
import java.util.List;

/**
 * This is the manager for the PortalPageService
 *
 * @since v3.13
 */
public interface PortalPageManager extends SharedEntityAccessor<PortalPage>
{
    /**
     * Returns the PortalPage with the specified portalPageId, IF the user is allowed to see it.
     *
     * @param user         the User in play
     * @param portalPageId the id of the PortalPage to return
     * @return a PortalPage if the user is allowed to see it or null if they are not allowed
     */
    PortalPage getPortalPage(final ApplicationUser user, final Long portalPageId);

    /**
     * @deprecated Use {@link #getPortalPage(ApplicationUser, Long)} instead. Since v6.0.
     *
     * Returns the PortalPage with the specified portalPageId, IF the user is allowed to see it.
     *
     * @param user         the User in play
     * @param portalPageId the id of the PortalPage to return
     * @return a PortalPage if the user is allowed to see it or null if they are not allowed
     */
    PortalPage getPortalPage(final User user, final Long portalPageId);

    /**
     *
     * Returns a Collection of PortalPage objects that are owned by the specified User
     *
     * @param owner the User who owns the PortalPage's
     * @return a Collection of PortalPage objects that are owned by the specified User
     */
    Collection<PortalPage> getAllOwnedPortalPages(final ApplicationUser owner);

    /**
     * @deprecated Use {@link #getAllOwnedPortalPages(ApplicationUser)} instead. Since v6.0.
     *
     * Returns a Collection of PortalPage objects that are owned by the specified User
     *
     * @param owner the User who owns the PortalPage's
     * @return a Collection of PortalPage objects that are owned by the specified User
     */
    Collection<PortalPage> getAllOwnedPortalPages(final User owner);

    /**
     * Get all PortalPages.
     *
     * @return an {@link com.atlassian.jira.util.collect.EnclosedIterable} of PortalPages
     */
    EnclosedIterable<PortalPage> getAll();

    /**
     * Gets a PortalPage by id regardless of owner
     *
     * @param portalPageId the id of the PortalPage to retrieve
     * @return a PortalPage object or null if it cant be found
     */
    PortalPage getPortalPageById(final Long portalPageId);

    /**
     * Returns a PortalPage by searching for it by owner and pageName.
     *
     * @param owner    the owner in play
     * @param pageName the name of the PortalPage
     * @return a PortalPage object if one can be found with the name and owner or null if not
     */
    PortalPage getPortalPageByName(final ApplicationUser owner, final String pageName);

    /**
     * @deprecated Use {@link #getPortalPageByName(com.atlassian.jira.user.ApplicationUser, String)} instead. Since v6.0.
     *
     * Returns a PortalPage by searching for it by owner and pageName.
     *
     * @param owner    the owner in play
     * @param pageName the name of the PortalPage
     * @return a PortalPage object if one can be found with the name and owner or null if not
     */
    PortalPage getPortalPageByName(final User owner, final String pageName);

    /**
     * Returns the system default PortalPage. This has no owner and is used when a User has no PortalPage objects defined for them
     *
     * @return a non null system default PortalPage object
     */
    PortalPage getSystemDefaultPortalPage();

    /**
     * Creates the specified PortalPage in the database
     *
     * @param portalPage the PortalPage to create
     * @return the newly created PortalPage with its new id
     */
    PortalPage create(PortalPage portalPage);

    /**
     * Creates a PortalPage in the database by cloning its Portlet content from the specified clonePortalPage
     *
     * @param pageOwner       the User who will own the cloned portal page
     * @param portalPage      the PortalPage to create
     * @param clonePortalPage the PortalPage to clone Portlet content from
     * @return the newly created PortalPage with its new id
     */
    PortalPage createBasedOnClone(final ApplicationUser pageOwner, final PortalPage portalPage, final PortalPage clonePortalPage);

    /**
     * @deprecated Use {@link #createBasedOnClone(com.atlassian.jira.user.ApplicationUser, PortalPage, PortalPage)} instead. Since v6.0.
     *
     * Creates a PortalPage in the database by cloning its Portlet content from the specified clonePortalPage
     *
     * @param pageOwner       the User who will own the cloned portal page
     * @param portalPage      the PortalPage to create
     * @param clonePortalPage the PortalPage to clone Portlet content from
     * @return the newly created PortalPage with its new id
     */
    PortalPage createBasedOnClone(final User pageOwner, final PortalPage portalPage, final PortalPage clonePortalPage);

    /**
     * Updates the specified PortalPage in the database
     *
     * @param portalPage the PortalPage to update
     * @return the newly updated PortalPage
     */
    PortalPage update(PortalPage portalPage);

    /**
     * Deletes the PortalPage with the specified portalPageId
     *
     * @param portalPageId the id of the PortalPage to delete
     */
    void delete(Long portalPageId);

    /**
     * This can be called to save a {@link PortletConfiguration} object
     * inside a {@link PortalPage} object to the underlying database store.
     *
     * @param portletConfiguration The portletConfiguration to save.
     */
    void saveLegacyPortletConfiguration(PortletConfiguration portletConfiguration);

    /**
     * Search for the PortalPages that match the passed SearchParameters. The result can be paged so that a subset of the results can be returned.
     *
     * @param searchParameters the SearchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result containing the PortalPages objects that match the request.
     */
    SharedEntitySearchResult<PortalPage> search(SharedEntitySearchParameters searchParameters, ApplicationUser user, int pagePosition, int pageWidth);

    /**
     * @deprecated Use {@link #search(com.atlassian.jira.sharing.search.SharedEntitySearchParameters, com.atlassian.jira.user.ApplicationUser, int, int)} instead. Since v6.0.
     *
     * Search for the PortalPages that match the passed SearchParameters. The result can be paged so that a subset of the results can be returned.
     *
     * @param searchParameters the SearchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result containing the PortalPages objects that match the request.
     */
    SharedEntitySearchResult<PortalPage> search(SharedEntitySearchParameters searchParameters, User user, int pagePosition, int pageWidth);

    /**
     * Returns a sorted immutable list of lists of portletconfigurations for a particular portal page.  The outer list
     * represents columns and the inner lists represent rows within a column.  This method will return portlet configs
     * sorted correctly as returned by the underlying persistance layer.
     *
     * @param portalPageId The portal page id to fetch portlet configs for
     * @return immutable list of lists representing portletconfigs on a dashboard
     */
    List<List<PortletConfiguration>> getPortletConfigurations(final Long portalPageId);
}
