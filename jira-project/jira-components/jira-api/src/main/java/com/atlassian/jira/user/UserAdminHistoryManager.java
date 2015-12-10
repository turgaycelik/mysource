package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * A wrapper of the {@link com.atlassian.jira.user.UserHistoryManager} that allows you to deal directly with Admin
 * pages
 *
 * @since v4.1
 */
public interface UserAdminHistoryManager
{
    public static int DEFAULT_ADMIN_HISTORY_DROPDOWN_ITEMS = 5;


    /**
     * Add an admin page to the user history list. A null users history should still be stored, even if only for
     * duration of session
     *
     * @param user The user to add the history item to
     * @param key The link id of the admin page to add to history
     * @param data The url of the link (for breaking ties between sections)
     */
    void addAdminPageToHistory(User user, String key, String data);

    /**
     * Retreive the user's admin page history queue. The list is returned ordered by DESC lastViewed date (i.e. newest
     * is first). This method performs no permission checks.  And is extremely fast. Admin pages don't have permissions
     * per se, and depend on the WebItemModuleDescriptor rendering the page link to decide permissions
     *
     * @param user The user to get the history admin page items for.
     * @return a list of history admin page items sort by desc lastViewed date.
     */
    @Nonnull
    List<UserHistoryItem> getAdminPageHistoryWithoutPermissionChecks(User user);
}