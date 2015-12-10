package com.atlassian.jira.user;


import com.atlassian.crowd.embedded.api.User;

import java.util.List;

/**
 * A wrapper of the {@link UserHistoryManager} that allows you to store and retrieve history items {@link com.atlassian.jira.user.UserHistoryItem} of the type JQL_QUERY.
 *
 * @since v4.0
 */
public interface UserQueryHistoryManager
{
    /**
     * Add a JQL query string to the user history list.
     *
     * @param user      The user to add the history item to
     * @param query     The JQL Query string to store in this history.
     */
    void addQueryToHistory(User user, String query);

    /**
     * Retrieve the user's query history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     *
     * @param user  The user to get the history query items for.
     * @return a list of history query items sort by desc lastViewed date.
     */
    List<UserHistoryItem> getUserQueryHistory(User user);

}
