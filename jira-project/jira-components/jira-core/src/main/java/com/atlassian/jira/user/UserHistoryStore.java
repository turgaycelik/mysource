package com.atlassian.jira.user;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import javax.annotation.Nonnull;

/**
 * Store interface for {@link com.atlassian.jira.user.UserHistoryItem} objects.
 *
 * @since v4.0
 */
public interface UserHistoryStore
{
    /**
     * Add a history item to the database.  This removes the currently referred to entity (user, type, id) from the
     * list and then adds it.  If adding it causes the history items stored for that user/type to exceed the
     * max (jira.max.history.items) items allowed, it should remove the oldest items.
     *
     * @param user        The user to store the history item against
     * @param historyItem the item to store. Containing a timestamp and referenced entity
     */
    void addHistoryItem(@Nullable ApplicationUser user, @Nonnull UserHistoryItem historyItem);

    /**
     * Retrieve the history for a given user/type.
     *
     * @param type The type of entity to retrieve history for.
     * @param user The user to retrieve history for.
     * @return a list containing all stored history items for the passed in user/type.
     */
    @Nonnull
    List<UserHistoryItem> getHistory(@Nonnull UserHistoryItem.Type type, @Nonnull ApplicationUser user);

    /**
     * Remove all history items for a given user.
     *
     * @param user The user to remove all history of.
     * @return The set of history types that were removed;
     */
    Set<UserHistoryItem.Type> removeHistoryForUser(@Nonnull ApplicationUser user);

    /**
     * Method for removing old user history items. Removes elements older than provided timestamp,
     * but not younger than 30 days.
     *
     * @param timestamp remove elements older than this timestamp
     * @throws java.lang.IllegalArgumentException If provided timestamp is not at least 30 days old
     */
    void removeHistoryOlderThan(@Nonnull final Long timestamp);

    /**
     * Retrieve the history for a given user/type.
     *
     * @param type The type of entity to retrieve history for.
     * @param userKey The key of the user to retrieve history for.
     * @return a list containing all stored history items for the passed in user/type.
     */
    @Nonnull
    List<UserHistoryItem> getHistory(@Nonnull UserHistoryItem.Type type, @Nonnull String userKey);
}
