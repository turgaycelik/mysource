package com.atlassian.jira.issue.watchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Allows watching of issues. I.e.: Users watching an issue will receive
 * notifications for every update of the issue.
 */
@PublicApi
public interface WatcherManager
{
    boolean isWatchingEnabled();

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     * @deprecated Use {@link #isWatching(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    boolean isWatching(User user, Issue issue);

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     */
    boolean isWatching(ApplicationUser user, Issue issue);

    /**
     * Determine whether the current user is already watching the issue or not
     *
     * @param user  user
     * @param issue issue being watched
     * @return True if a user is watching the issue specified.
     * @deprecated Use {@link #isWatching(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    boolean isWatching(User user, GenericValue issue);

    /**
     * Retrieve collection of users that are currently watching this issue (including the current user)
     *
     * @param userLocale the locale of the user making this call, this is used for sorting the list values.
     * @param issue      issue being watched
     * @return A collection of {@link User}s
     * @since v4.3
     * @deprecated Use {@link #getWatchers(com.atlassian.jira.issue.Issue, java.util.Locale)} instead. Since v6.0.
     */
    Collection<User> getCurrentWatchList(Issue issue, Locale userLocale);

    /**
     * Retrieve list of users that are currently watching this issue (including the current user).
     *
     * @param userLocale the locale of the user making this call, this is used for sorting the list values.
     * @param issue      issue being watched
     * @return list of users that are currently watching this issue (including the current user)
     * @since v6.0
     */
    List<ApplicationUser> getWatchers(Issue issue, Locale userLocale);

    /**
     * Returns the number of users watching this issue.
     *
     * @param issue issue being watched
     * @return the number of users watching this issue.
     * @since v6.0
     */
    int getWatcherCount(Issue issue);

    /**
     * Retrieve list of users that are currently watching this issue (including the current user).
     *
     * @param issue issue being watched
     * @return list of users that are currently watching this issue (including the current user)
     * @since v6.0
     */
    Collection<String> getWatcherUserKeys(Issue issue);

    /**
     * Retrieve the list of usernames of users watching the given issue
     *
     * @param issue issue being watched
     * @return the list of usernames of users watching the given issue
     * @throws DataAccessException if cannot retrieve watchers
     */
    List<String> getCurrentWatcherUsernames(Issue issue) throws DataAccessException;

    /**
     * Retrieve the list of usernames of users watching the given issue
     *
     * @param issue issue being watched
     * @return the list of usernames of users watching the given issue
     * @throws DataAccessException if cannot retrieve watchers
     * @deprecated Use {@link #getCurrentWatcherUsernames(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    List<String> getCurrentWatcherUsernames(GenericValue issue) throws DataAccessException;

    /**
     * Enable watching of a particular issue for the user supplied.
     * <p/>
     * This means the user will receive updates for any modifications to the issue.
     * Note, that this will not check if a user has the BROWSE_ISSUE permission.
     * Notifications will however only be sent to users who have the appropriate permissions.
     * Adding a permission check here would complicate updating permission schemes a lot, as
     * it would have to update issue's watchers lists.
     *
     * @param user  user that starts watching the given issue
     * @param issue issue being watched
     * @deprecated Use {@link #startWatching(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    void startWatching(final User user, final Issue issue);

    /**
     * Enable watching of a particular issue for the user supplied.
     * <p/>
     * This means the user will receive updates for any modifications to the issue.
     * Note, that this will not check if a user has the BROWSE_ISSUE permission.
     * Notifications will however only be sent to users who have the appropriate permissions.
     * Adding a permission check here would complicate updating permission schemes a lot, as
     * it would have to update issue's watchers lists.
     *
     * @param user  user that starts watching the given issue
     * @param issue issue being watched
     */
    void startWatching(final ApplicationUser user, final Issue issue);

    /**
     * Enable watching of a list of issues for the user supplied.
     * <p/>
     * This means the user will receive updates for any modifications to the issues.
     * Note, that this will not check if a user has the BROWSE_ISSUE permission.
     * Notifications will however only be sent to users who have the appropriate permissions.
     * Adding a permission check here would complicate updating permission schemes a lot,
     * as it would have to update issues' watchers lists.
     * <p/>
     * This bulk method is more performant than calling the single version
     * multiple times, as it indexes the issues in bulk rather than one at a time.
     *
     * @param user   user that starts watching the given issues
     * @param issues the list of issues to watch
     * @deprecated Since 6.3.6 use {@link #startWatching(com.atlassian.jira.user.ApplicationUser, java.util.Collection, com.atlassian.jira.task.context.Context)}
     */
    @Deprecated
    void startWatching(final ApplicationUser user, final Collection<Issue> issues);

    /**
     * Enable watching of a list of issues for the user supplied.
     * <p/>
     * This means the user will receive updates for any modifications to the issues.
     * Note, that this will not check if a user has the BROWSE_ISSUE permission.
     * Notifications will however only be sent to users who have the appropriate permissions.
     * Adding a permission check here would complicate updating permission schemes a lot,
     * as it would have to update issues' watchers lists.
     * <p/>
     * This bulk method is more performant than calling the single version
     * multiple times, as it indexes the issues in bulk rather than one at a time.
     *
     * @param user        user that starts watching the given issues
     * @param issues      the list of issues to watch
     * @param taskContext a context through which progress can be reported back
     */
    void startWatching(final ApplicationUser user, final Collection<Issue> issues, final Context taskContext);

    /**
     * Enable watching of a particular issue for the user supplied.
     * <p/>
     * This means the user will receive updates for any modifications to the issue.
     * Note, that this will not check if a user has the BROWSE_ISSUE permission.
     * Notifications will however only be sent to users who have the appropriate permissions.
     * Adding a permission check here would complicate updating permission schemes a lot,
     * as it would have to update issue's watchers lists.
     *
     * @param user  user that starts watching the given issue
     * @param issue issue being watched
     * @deprecated Use {@link #startWatching(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    void startWatching(User user, GenericValue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     * @deprecated Use {@link #stopWatching(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.Issue)} instead. Since v6.0.
     */
    void stopWatching(User user, Issue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     */
    void stopWatching(ApplicationUser user, Issue issue);

    /**
     * Disable watching of a list of issues for the user supplied.
     * <p/>
     * This bulk method is more performant than calling the single version
     * multiple times, as it indexes the issues in bulk rather than one at a time.
     *
     * @param user   user that stops watching the given issues
     * @param issues list of issues being watched
     * @deprecated Since 6.3.6 use {@link #stopWatching(com.atlassian.jira.user.ApplicationUser, java.util.Collection, com.atlassian.jira.task.context.Context)}
     */
    @Deprecated
    void stopWatching(ApplicationUser user, Collection<Issue> issues);

    /**
     * Disable watching of a list of issues for the user supplied.
     * <p/>
     * This bulk method is more performant than calling the single version
     * multiple times, as it indexes the issues in bulk rather than one at a time.
     *
     * @param user        user that stops watching the given issues
     * @param issues      list of issues being watched
     * @param taskContext a context through which progress can be reported back
     */
    void stopWatching(ApplicationUser user, Collection<Issue> issues, Context taskContext);

    /**
     * Disable watching of a particular issue for the user supplied.
     *
     * @param user  user that stops watching the given issue
     * @param issue issue being watched
     * @deprecated Use {@link #stopWatching(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    void stopWatching(User user, GenericValue issue);

    /**
     * Disable watching of a particular issue for the user supplied.
     * <p/>
     * Note: Use this method in case when user no longer exists in JIRA, e.g.
     * JIRA uses external user management and user was removed externally.
     *
     * @param username username of the user that stops watching the given issue
     * @param issue    issue being watched
     * @deprecated Use {@link #stopWatching(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    void stopWatching(String username, GenericValue issue);

    /**
     * Remove all watches for a given user
     *
     * @param user The user that has most probably been  deleted
     * @since v3.13
     * @deprecated Use {@link #removeAllWatchesForUser(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    void removeAllWatchesForUser(User user);

    /**
     * Remove all watches for a given user
     *
     * @param user The user that has most probably been  deleted
     * @since v6.0
     */
    void removeAllWatchesForUser(ApplicationUser user);
}
