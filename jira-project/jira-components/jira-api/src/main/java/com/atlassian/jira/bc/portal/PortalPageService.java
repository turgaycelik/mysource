package com.atlassian.jira.bc.portal;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.List;

/**
 * The PortalPageService is the top level service for Dashboard operations. <p/> For historical reasons, Dashboards where called Portals and hence the
 * prefix "Portal" is used consistently throughout the code.
 *
 * @since v3.13
 */
@PublicApi
public interface PortalPageService
{
    /**
     * Retrieve a given {@link com.atlassian.jira.portal.PortalPage} by id.
     *
     * @param servceCtx    JIRA Service context containing an error collection and user requesting (to run) the
     *                     {@link com.atlassian.jira.portal.PortalPage}. The {@link com.atlassian.jira.portal.PortalPage} must exist and the user must be able to
     *                     see {@link com.atlassian.jira.portal.PortalPage} else an error will result.
     * @param portalPageId The id of the {@link com.atlassian.jira.portal.PortalPage} to retrieve. Id must not be null.
     * @return The PortalPage as specified by the id, or null if none exists for the user.
     */
    PortalPage getPortalPage(final JiraServiceContext servceCtx, final Long portalPageId);

    /**
     * Retrieve all {@link PortalPage}'s a user has favourited. Permission checks are done to ensure the user can see the {@link PortalPage}, as
     * visibility may have been removed from underneath them.
     *
     * @param user The user who has favourite {@link PortalPage}'s. Also to test visibility and with
     * @return a Collection of {@link PortalPage} objects that represent {@link PortalPage}'s the user has favourited.
     */
    Collection<PortalPage> getFavouritePortalPages(final ApplicationUser user);

    /**
     * @deprecated Use {@link #getFavouritePortalPages(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Retrieve all {@link PortalPage}'s a user has favourited. Permission checks are done to ensure the user can see the {@link PortalPage}, as
     * visibility may have been removed from underneath them.
     *
     * @param user The user who has favourite {@link PortalPage}'s. Also to test visibility and with
     * @return a Collection of {@link PortalPage} objects that represent {@link PortalPage}'s the user has favourited.
     */
    Collection<PortalPage> getFavouritePortalPages(final User user);

    /**
     * Is the passed PortalPage a favourite of the passed User.
     *
     * @param user the user to check.
     * @param portalPage the page to check.
     *
     * @return true if the PortalPage is a favourite of the passed User.
     */
    boolean isFavourite(ApplicationUser user, PortalPage portalPage);

    /**
     * @deprecated Use {@link #isFavourite(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.portal.PortalPage)} instead. Since v6.0.
     *
     * Is the passed PortalPage a favourite of the passed User.
     *
     * @param user the user to check.
     * @param portalPage the page to check.
     *
     * @return true if the PortalPage is a favourite of the passed User.
     */
    boolean isFavourite(User user, PortalPage portalPage);

    /**
     * Retrieve all {@link PortalPage}'s a user owns/has created.
     *
     * @param user The user who created the {@link PortalPage}'s.
     * @return a Collection of {@link PortalPage} objects that represent {@link PortalPage}'s the user has created.
     */
    Collection<PortalPage> getOwnedPortalPages(final ApplicationUser user);

    /**
     * @deprecated Use {@link #getOwnedPortalPages(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Retrieve all {@link PortalPage}'s a user owns/has created.
     *
     * @param user The user who created the {@link PortalPage}'s.
     * @return a Collection of {@link PortalPage} objects that represent {@link PortalPage}'s the user has created.
     */
    Collection<PortalPage> getOwnedPortalPages(final User user);

    /**
     * Get a user's non private {@link PortalPage}'s. I.e. {@link PortalPage}'s that other users can possibly see.
     *
     * @param user The author of the {@link PortalPage}'s
     * @return Collection of PortalPage objects that do not have private scope.
     */
    Collection<PortalPage> getNonPrivatePortalPages(final ApplicationUser user);

    /**
     * @deprecated Use {@link #getNonPrivatePortalPages(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Get a user's non private {@link PortalPage}'s. I.e. {@link PortalPage}'s that other users can possibly see.
     *
     * @param user The author of the {@link PortalPage}'s
     * @return Collection of PortalPage objects that do not have private scope.
     */
    Collection<PortalPage> getNonPrivatePortalPages(final User user);

    /**
     * Get {@link PortalPage}'s owned by a given user that have been favourited by at least one other user
     *
     * @param user The author of the {@link PortalPage}'s
     * @return Collection of PortalPage objects owned by the given user and favourited by at least one other user
     */
    Collection<PortalPage> getPortalPagesFavouritedByOthers(final ApplicationUser user);

    /**
     * @deprecated Use {@link #getPortalPagesFavouritedByOthers(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Get {@link PortalPage}'s owned by a given user that have been favourited by at least one other user
     *
     * @param user The author of the {@link PortalPage}'s
     * @return Collection of PortalPage objects owned by the given user and favourited by at least one other user
     */
    Collection<PortalPage> getPortalPagesFavouritedByOthers(final User user);

    /**
     * This returns the System Default PortalPage as defined by the JIRA Administrators
     *
     * @return the system default PortalPage
     */
    public PortalPage getSystemDefaultPortalPage();

    /**
     * Called to validate that the PortalPage object is in a valid state for creation. Call this before calling the create method .
     *
     * @param serviceCtx the JiraServiceContext in play
     * @param portalPage the PortalPage object that is to be validated for create
     * @return true if the PortalPage is valid or false. The ErrorCollection of the serviceCtx will have any errors in it.
     */
    boolean validateForCreate(JiraServiceContext serviceCtx, PortalPage portalPage);

    /**
     * Called to create and store the specified portalPage into the database.
     *
     * @param serviceCtx the JiraServiceContext in play
     * @param portalPage the PortalPage object that is to be created
     * @return a fully fledged PortalPage with the new Id in it.
     */
    PortalPage createPortalPage(JiraServiceContext serviceCtx, PortalPage portalPage);

    /**
     * Called to create and store the specified portalPage into the database.
     *
     * @param serviceCtx  the JiraServiceContext in play
     * @param portalPage  the PortalPage object that is to be created
     * @param isFavourite set to true if the PortalPage should be marked as a favourite
     * @return a fully fledged PortalPage with the new Id in it.
     */
    PortalPage createPortalPage(JiraServiceContext serviceCtx, PortalPage portalPage, boolean isFavourite);

    /**
     * Called to validate that a new PortalPage can be created by cloning its Portlet content from an existing PortalPage object.
     *
     * @param serviceCtx        the JiraServiceContext in play
     * @param portalPage        the PortalPage object that is to be created
     * @param clonePortalPageId the id of an existing PortalPage to clone
     * @return true if the PortalPage can be cloned from another page
     */
    boolean validateForCreatePortalPageByClone(JiraServiceContext serviceCtx, PortalPage portalPage, Long clonePortalPageId);

    /**
     * Called to create and store the specified portalPage into the database by cloning its Portlet content from an existing PortalPage object.
     *
     * @param serviceCtx        the JiraServiceContext in play
     * @param portalPage        the PortalPage object that is to be created
     * @param clonePortalPageId the id of an existing PortalPage to clone
     * @param isFavourite       set to true if the PortalPage should be marked as a favourite
     * @return a fully fledged PortalPage with the new Id in it.
     */
    PortalPage createPortalPageByClone(JiraServiceContext serviceCtx, PortalPage portalPage, Long clonePortalPageId, boolean isFavourite);

    /**
     * Called to validate that the specified portalPage with the portalPageId can be deleted.
     *
     * @param serviceCtx   the JiraServiceContext in play
     * @param portalPageId the id of the PortalPage to delete
     * @return true if the PortalPage can be deleted. The ErrorCollection of the serviceCtx will have any errors in it.
     */
    boolean validateForDelete(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Called to delete the PortalPage with the specified portalPageId.
     *
     * @param serviceCtx   the JiraServiceContext in play
     * @param portalPageId the id of the PortalPage to delete
     */
    void deletePortalPage(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Called to validate that the PortalPage can be be updated
     *
     * @param serviceCtx the JiraServiceContext in play
     * @param portalPage the PortalPage to update
     * @return true if the PortalPage is valid or false. The ErrorCollection of the serviceCtx will have any errors in it.
     */
    boolean validateForUpdate(JiraServiceContext serviceCtx, PortalPage portalPage);

    /**
     * Updates the specified PortalPage in the database
     *
     * @param serviceCtx  the JiraServiceContext in play
     * @param portalPage  the PortalPage to update
     * @param isFavourite set to true if the PortalPage is to be marked as a favourite
     * @return a newly updated PortalPage
     */
    PortalPage updatePortalPage(JiraServiceContext serviceCtx, PortalPage portalPage, boolean isFavourite);

    /**
     * Updates the specified PortalPage in the database , no permission checks are applied, but the user must be an administrator
     *
     * @param serviceCtx  the JiraServiceContext in play
     * @param portalPage  the PortalPage to update
     * @param user must be admin in order to make the change
     * @return a newly updated PortalPage
     */
    PortalPage updatePortalPageUnconditionally(JiraServiceContext serviceCtx, ApplicationUser user, PortalPage portalPage);

    /**
     * @deprecated Use {@link #updatePortalPageUnconditionally(com.atlassian.jira.bc.JiraServiceContext, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.portal.PortalPage)} instead. Since v6.0.
     *
     * Updates the specified PortalPage in the database , no permission checks are applied, but the user must be an administrator
     *
     * @param serviceCtx  the JiraServiceContext in play
     * @param portalPage  the PortalPage to update
     * @param user must be admin in order to make the change
     * @return a newly updated PortalPage
     */
    PortalPage updatePortalPageUnconditionally(JiraServiceContext serviceCtx, User user, PortalPage portalPage);

    /**
     * Called to validate that sequence of a PortalPage can be changed.
     *
     * @param serviceCtx   the context of the service call
     * @param portalPageId the id of the PortalPage to re-sequence
     * @return true if the PortalPage can be re-sequenced
     */
    boolean validateForChangePortalPageSequence(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Increases the position of the page configuration in the user's page configuration set
     *
     * @param serviceCtx   the context of the service call
     * @param portalPageId the id of the PortalPage to re-sequence
     */
    void increasePortalPageSequence(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Decreases the position of the page configuration in the user's page configuration set.
     *
     * @param serviceCtx   the context of the service call
     * @param portalPageId the id of the PortalPage to re-sequence
     */
    void decreasePortalPageSequence(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Puts the given page to the start of the user's page configuration set.
     *
     * @param serviceCtx   the context of the service call
     * @param portalPageId the id of the PortalPage to re-sequence
     */
    void moveToStartPortalPageSequence(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Puts the given page to the end of the user's page configuration set.
     *
     * @param serviceCtx   the context of the service call
     * @param portalPageId the id of the PortalPage to re-sequence
     */
    void moveToEndPortalPageSequence(JiraServiceContext serviceCtx, Long portalPageId);

    /**
     * Save a specific {@link com.atlassian.jira.portal.PortletConfiguration} to a
     * database store.
     *
     * @param serviceCtx      the context of the service call
     * @param portletConfiguration The portletConfiguration to update
     */
    void saveLegacyPortletConfiguration(JiraServiceContext serviceCtx, PortletConfiguration portletConfiguration);

    /**
     * This will validate that the input parameters are valid for a search that encompasses ANY share entity type.
     *
     * @param serviceCtx       Context containing user, error collection and i18n bean
     * @param searchParameters the SharedEntitySearchParameters to validate
     */
    void validateForSearch(JiraServiceContext serviceCtx, SharedEntitySearchParameters searchParameters);

    /**
     * Search for the PortalPages that match the passed SearchParameters. The result can be paged so that a subset of the results can be returned.
     *
     * @param serviceCtx       Context containing user, error collection and i18n bean
     * @param searchParameters the searchParameters to query.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<PortalPage> search(JiraServiceContext serviceCtx, SharedEntitySearchParameters searchParameters, int pagePosition, int pageWidth);

    /**
     * Delete all Portal Pages owned by a user.  This method will also remove all favourites for all portlets as well as
     * remove all favourites for the user.  A nice big cleanup method for when deleting a user.
     *
     * @param user The user to clean up after.
     */
    void deleteAllPortalPagesForUser(ApplicationUser user);

    /**
     * @deprecated Use {@link #deleteAllPortalPagesForUser(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Delete all Portal Pages owned by a user.  This method will also remove all favourites for all portlets as well as
     * remove all favourites for the user.  A nice big cleanup method for when deleting a user.
     *
     * @param user The user to clean up after.
     */
    void deleteAllPortalPagesForUser(User user);

    /**
     * Check if the user in the service context has permission to get the portal page with the id provided.
     *
     * @param context Context containing user, error collection and i18n bean
     * @param portalPageId The id of the portal page to get
     * @return true if the user has permission, false if the user has no permission, or if the page doesn't exist.
     */
    boolean validateForGetPortalPage(final JiraServiceContext context, final Long portalPageId);

    /**
     * Returns all portlet configurations for a particular dashboard in colums and rows.  The portlet configurations
     * are returned sorted correctly in each column by row.
     *
     * @param context Context containing user, error collection and i18n bean
     * @param portalPageId The id of the portal page to get
     * @return A list of lists representing all portlet configs in all columns for the gadget in question.
     */
    List<List<PortletConfiguration>> getPortletConfigurations(final JiraServiceContext context, final Long portalPageId);

    /**
     * This will validate that the dashboard page can be changed to the new owbner.
     *
     * @param ctx       Context containing user, error collection and i18n bean
     * @param dashboard  the PortalPage to validate
     */
    void validateForChangeOwner(JiraServiceContext ctx, PortalPage dashboard);

}
