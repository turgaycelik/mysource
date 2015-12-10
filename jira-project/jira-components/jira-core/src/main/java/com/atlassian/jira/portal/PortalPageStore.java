package com.atlassian.jira.portal;

import java.util.Collection;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.EnclosedIterable;

/**
 * This is the store for PortalPage objects
 *
 * @since v3.13
 */
public interface PortalPageStore
{
    /**
     * Goes to the database and fetches the system default dashboard configuration.
     *
     * @return the system default portal page.
     */
    PortalPage getSystemDefaultPortalPage();

    /**
     * Get a {@link EnclosedIterable} of PortalPages for the specified List of ids.
     *
     * @param descriptor retrieval descriptor
     * @return CloseableIterable that contains reference to PortalPages with the specified ids.
     */
    EnclosedIterable<PortalPage> get(RetrievalDescriptor descriptor);

    /**
     * Get a {@link EnclosedIterable} of all PortalPages in the database.
     *
     * @return CloseableIterable that contains reference to all PortalPages.
     */
    EnclosedIterable<PortalPage> getAll();

    /**
     * Gets all the PortalPage's owned by the specified User
     *
     * @param owner the user who is the owner of the PortalPage's
     * @return a Collection of PortalPage objects owner by the User
     */
    Collection<PortalPage> getAllOwnedPortalPages(ApplicationUser owner);

    /**
     * Gets all the PortalPage's owned by the specified User
     *
     * @param userKey The key of  the user who is the owner of the PortalPage's
     * @return a Collection of PortalPage objects owner by the User
     */
    Collection<PortalPage> getAllOwnedPortalPages(String userKey);

    /**
     * Gets the specified PortalPage that is owned by the User and has the specified portalPageName
     *
     * @param owner          the User how is the owner of the PortalPage
     * @param portalPageName the name of the PortalPage
     * @return a PortalPage object or null if it cant be found
     */
    PortalPage getPortalPageByOwnerAndName(ApplicationUser owner, String portalPageName);

    /**
     * Gets the PortalPage with the specified portalPageId
     *
     * @param portalPageId the id of the PortalPage to locate
     * @return a PortalPage or null if it cant be found
     */
    PortalPage getPortalPage(Long portalPageId);

    /**
     * Creates a PortalPage in the database
     *
     * @param portalPage the PortalPage to create
     * @return the new PortalPage with its new database id
     */
    PortalPage create(PortalPage portalPage);

    /**
     * Updates the PortalPage in the database.
     *
     * @param portalPage the PortalPage to update
     * @return a newly updated PortalPage object
     */
    PortalPage update(PortalPage portalPage);

    /**
     * This method will increment the version of the given portalPage by one.  When doing this,
     * it will check that the version currently equals what was passed in.  If the update
     * is successful, this method returns true.  False otherwise
     *
     * In JIRA since we don't have transactions, this method should be called inside a pessimistic lock (potentially
     * striped by portalPageId) and all update operations should follow while holding this same lock.
     *
     * @param portalPageId The dashboard to update
     * @param currentVersion The current version for this dashboard
     * @return true if the update is successful, false otherwise
     */
    boolean updatePortalPageOptimisticLock(Long portalPageId, Long currentVersion);

    /**
     * Updates the favourite count of the PortalPage in the database.
     *
     * @param portalPage     the portal page to change.
     * @param incrementValue the value to increase the favourite count by. Can be a number < 0 to decrease the favourite count.
     * @return a newly updated PortalPage object.
     */
    PortalPage adjustFavouriteCount(SharedEntity portalPage, int incrementValue);

    /**
     * Deletes the PortalPage with the specified portalPageId
     *
     * @param portalPageId the id of the PortalPage to delete
     */
    void delete(Long portalPageId);

    /**
     * Flushes any caches that may exist
     */
    void flush();
}
