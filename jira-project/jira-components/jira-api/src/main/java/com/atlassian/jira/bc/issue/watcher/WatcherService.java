package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.lang.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Watcher-related business logic interface.
 *
 * @since v4.2
 */
@PublicApi
public interface WatcherService
{
    /**
     * Returns a boolean indicating whether watching is enabled in JIRA.
     *
     * @return a boolean indicating whether watching is enabled
     */
    boolean isWatchingEnabled();

    /**
     * Returns a boolean indicating whether the given user is authorised to view an issue's watcher list.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user is authorised to view the watcher list
     */
    boolean hasViewWatcherListPermission(Issue issue, User remoteUser);

    /**
     * Returns a the total number of watchers for a given issue in the first element of the returned Pair, and the list
     * of visible watchers in the second element of the Pair. Note that if the remote user does not have permission to
     * view the list of watchers, it is possible for the number of elements in the returned user list to be less than
     * the returned number of watchers.
     *
     * @param issue the Issue to find watchers for
     * @param remoteUser the calling User
     * @return a ServiceOutcome containing the total number of watchers, and a list of visible watchers
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<Pair<Integer, List<User>>> getWatchers(Issue issue, User remoteUser)
            throws WatchingDisabledException;

    /**
     * Adds a watcher to an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the issue to update
     * @param remoteUser the remote user on behalf of which the operation is performed
     * @param watcher the watcher to add
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<List<User>> addWatcher(Issue issue, User remoteUser, User watcher) throws WatchingDisabledException;

    /**
     * Adds a watcher to all of the supplied issues.
     * <p/>
     * If there is partial success, the issues which we can modify will
     * be modified and the ones we cannot will be returned in a BulkWatchResult.
     *
     * @param issues the list of issues to update
     * @param remoteUser the remote user on behalf of which the operation is performed
     * @param watcher the watcher to add
     * @return a BulkWatchResult containing the issues that could not be modified
     * @throws WatchingDisabledException if watching is currently disabled
     */
    BulkWatchResult addWatcherToAll(Collection<Issue> issues, ApplicationUser remoteUser, ApplicationUser watcher)
            throws WatchingDisabledException;

    /**
     * Adds a watcher to all of the supplied issues.
     * <p/>
     * If there is partial success, the issues which we can modify will
     * be modified and the ones we cannot will be returned in a BulkWatchResult.
     *
     * @param issues the list of issues to update
     * @param remoteUser the remote user on behalf of which the operation is performed
     * @param watcher the watcher to add
     * @param taskContext a context through which progress can be reported back
     * @return a BulkWatchResult containing the issues that could not be modified
     * @throws WatchingDisabledException if watching is currently disabled
     */
    BulkWatchResult addWatcherToAll(Collection<Issue> issues, ApplicationUser remoteUser, ApplicationUser watcher,
            Context taskContext) throws WatchingDisabledException;

    /**
     * Removes a watcher from an issue's list of watchers, returning the updated list of watchers.
     *
     * @param issue the Issue to update
     * @param remoteUser a User indicating the user on behalf of whom this operation is being performed
     * @param watcher a User representing the User to remove from the watcher list
     * @return a ServiceOutcome containing a list of User
     * @throws WatchingDisabledException if watching is currently disabled
     */
    ServiceOutcome<List<User>> removeWatcher(Issue issue, User remoteUser, User watcher)
            throws WatchingDisabledException;

    /**
     * Removes a watcher from all of the supplied issues.
     * <p/>
     * If there is partial success, the issues which we can modify
     * will be modified and the ones we cannot will be returned in a BulkWatchResult.
     *
     * @param issues the list of Issues to update
     * @param remoteUser an ApplicationUser indicating the user on behalf of whom this operation is being performed
     * @param watcher an ApplicationUser representing the user to remove from the watcher list for each issue
     * @return a BulkWatchResult containing the issues that could not be modified
     * @throws WatchingDisabledException if watching is currently disabled
     */
    BulkWatchResult removeWatcherFromAll(Collection<Issue> issues, ApplicationUser remoteUser, ApplicationUser watcher)
            throws WatchingDisabledException;

    /**
     * Removes a watcher from all of the supplied issues.
     * <p/>
     * If there is partial success, the issues which we can modify
     * will be modified and the ones we cannot will be returned in a BulkWatchResult.
     *
     * @param issues the list of Issues to update
     * @param remoteUser an ApplicationUser indicating the user on behalf of whom this operation is being performed
     * @param watcher an ApplicationUser representing the user to remove from the watcher list for each issue
     * @param taskContext a context through which progress can be reported back
     * @return a BulkWatchResult containing the issues that could not be modified
     * @throws WatchingDisabledException if watching is currently disabled
     */
    BulkWatchResult removeWatcherFromAll(Collection<Issue> issues, ApplicationUser remoteUser, ApplicationUser watcher,
            Context taskContext) throws WatchingDisabledException;

    /**
     * Whether the specified user can watch all the specified issues.
     *
     *
     * @param issues The list of issues to check
     * @param remoteUser The user to execute this check for
     * @return true; if the specified user can watch all the specified issues; otherwise, false is returned.
     * @deprecated since 6.1 use {@link #canWatchAll(Iterable, com.atlassian.jira.user.ApplicationUser)}
     */
    @Deprecated
    boolean canWatchAll(Iterable<Issue> issues, User remoteUser);

    /**
     * Whether the specified user can watch all the specified issues.
     *
     *
     * @param issues The list of issues to check
     * @param applicationUser The user to execute this check for
     * @return true; if the specified user can watch all the specified issues; otherwise, false is returned.
     */
    boolean canWatchAll(Iterable<Issue> issues, ApplicationUser applicationUser);

    /**
     * Whether the specified user can unwatch all the specified issues.
     *
     *
     * @param issues The list of issues to check
     * @param remoteUser The user to execute this check for
     * @return true; if the specified user can unwatch all the specified issues; otherwise, false is returned.
     * @deprecated since 6.1 use {@link #canUnwatchAll(Iterable, com.atlassian.jira.user.ApplicationUser)}
     */
    @Deprecated
    boolean canUnwatchAll(Iterable<Issue> issues, User remoteUser);

    /**
     * Whether the specified user can unwatch all the specified issues.
     *
     *
     * @param issues The list of issues to check
     * @param applicationUser The user to execute this check for
     * @return true; if the specified user can unwatch all the specified issues; otherwise, false is returned.
     */
    boolean canUnwatchAll(Iterable<Issue> issues, ApplicationUser applicationUser);

    /**
     * Used to return the result of a Bulk Watch or Bulk Unwatch operation.
     *
     * @since v6.0
    */
    static class BulkWatchResult
    {
        private final Collection<Pair<Issue, String>> failedIssues;

        @Internal
        BulkWatchResult(Collection<Pair<Issue, String>> failedIssues)
        {
            this.failedIssues = failedIssues;
        }

        /**
         * Returns the details of the issues that failed the bulk watch or unwatch procedure.
         *
         * @return a collection of Pair of type &lt;Issue, String&gt; - where each Pair contains
         * the issue that failed the operation, and a user-visible i18ned error string containing the reason
         * for the failure. If no issues failed then this will return an empty collection.
        */
        public Collection<Pair<Issue, String>> getFailedIssues()
        {
            return failedIssues;
        }
    }
}
