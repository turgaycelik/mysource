package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * The manager responsible for storing and retreiving {@link com.atlassian.jira.user.UserHistoryItem} objects.
 * Although it is possible to store a reference to any {@link com.atlassian.jira.user.UserHistoryItem.Type} it has
 * special methods for Issue history as that is the only use internal to JIRA.
 *
 * @since v4.0
 */
public interface UserHistoryManager
{
    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * A null users history should still be stored, even if only for duration of session.
     *
     * @param type     The type queue to add the history item to
     * @param user     The user to add the history item to
     * @param entity   The entity to add to the history queue.
     * @deprecated Use {@link #addItemToHistory(com.atlassian.jira.user.UserHistoryItem.Type, ApplicationUser, String)} instead. Since v6.0.
     */
    void addUserToHistory(UserHistoryItem.Type type, User user, User entity);

    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * A null users history should still be stored, even if only for duration of session.
     *
     * @param type     The type queue to add the history item to
     * @param user     The user to add the history item to
     * @param entity   The entity to add to the history queue.
     */
    void addUserToHistory(UserHistoryItem.Type type, ApplicationUser user, ApplicationUser entity);

    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * A null users history should still be stored, even if only for duration of session.
     *
     * @param type     The type queue to add the history item to
     * @param user     The user to add the history item to
     * @param entityId The entity id of the entity to add to the history queue.
     * @deprecated Use {@link #addItemToHistory(com.atlassian.jira.user.UserHistoryItem.Type, ApplicationUser, String)} instead. Since v6.0.
     */
    void addItemToHistory(UserHistoryItem.Type type, User user, String entityId);

    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * A null users history should still be stored, even if only for duration of session.
     *
     * @param type     The type queue to add the history item to
     * @param user     The user to add the history item to
     * @param entityId The entity id of the entity to add to the history queue.
     */
    void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId);

    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * Allows to store data related to the user history item.
     *
     * @param type      The type queue to add the history item to
     * @param user      The user to add the history item to
     * @param entityId  The entity id of the entity to add to the history queue
     * @param data      Data related to the history item. Can be null.
     * @deprecated Use {@link #addItemToHistory(com.atlassian.jira.user.UserHistoryItem.Type, ApplicationUser, String, String)} instead. Since v6.0.
     */
    void addItemToHistory(UserHistoryItem.Type type, User user, String entityId, String data);

    /**
     * Create and add an {@link com.atlassian.jira.user.UserHistoryItem} to the Users history list.
     * Allows to store data related to the user history item.
     *
     * @param type      The type queue to add the history item to
     * @param user      The user to add the history item to
     * @param entityId  The entity id of the entity to add to the history queue
     * @param data      Data related to the history item. Can be null.
     */
    void addItemToHistory(UserHistoryItem.Type type, ApplicationUser user, String entityId, String data);

    /**
     * Determines whether a user has any items in their history for a given {@link com.atlassian.jira.user.UserHistoryItem.Type}
     * This method performs no permission checks.
     *
     * @param type The type to check for
     * @param user The user to check for.
     * @return true if the user has any entities in their queue of the give type, false otherwise
     * @deprecated Use {@link #hasHistory(com.atlassian.jira.user.UserHistoryItem.Type, ApplicationUser)} instead. Since v6.0.
     */
    boolean hasHistory(UserHistoryItem.Type type, User user);

    /**
     * Determines whether a user has any items in their history for a given {@link com.atlassian.jira.user.UserHistoryItem.Type}
     * This method performs no permission checks.
     *
     * @param type The type to check for
     * @param user The user to check for.
     * @return true if the user has any entities in their queue of the give type, false otherwise
     */
    boolean hasHistory(UserHistoryItem.Type type, ApplicationUser user);

    /**
     * Retreive the user's history queue for the given {@link com.atlassian.jira.user.UserHistoryItem.Type}.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs no permission checks.
     *
     * @param type The type of entity to get the history for
     * @param user The user to get the history items for.
     * @return a list of history items sort by desc lastViewed date.
     * @deprecated Use {@link #getHistory(com.atlassian.jira.user.UserHistoryItem.Type, ApplicationUser)} instead. Since v6.0.
     */
    @Nonnull
    List<UserHistoryItem> getHistory(UserHistoryItem.Type type, User user);

    /**
     * Retreive the user's history queue for the given {@link com.atlassian.jira.user.UserHistoryItem.Type}.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs no permission checks.
     *
     * @param type The type of entity to get the history for
     * @param user The user to get the history items for.
     * @return a list of history items sort by desc lastViewed date.
     */
    @Nonnull
    List<UserHistoryItem> getHistory(UserHistoryItem.Type type, ApplicationUser user);

    /**
     * Remove the user's history.
     *
     * @param user The User to remove the history for.
     * @deprecated Use {@link #removeHistoryForUser(ApplicationUser)} instead. Since v6.0.
     */
    void removeHistoryForUser(@Nonnull User user);

    /**
     * Remove the user's history.
     *
     * @param user The User to remove the history for.
     */
    void removeHistoryForUser(@Nonnull ApplicationUser user);

}
