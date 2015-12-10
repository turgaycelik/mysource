package com.atlassian.jira.bc.issue.watcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;

/**
 * Implementation of WatcherService.
 *
 * @since v4.2
 */
public class DefaultWatcherService implements WatcherService
{
    /**
     * Logger for the DefaultWatcherService class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWatcherService.class);

    /**
     * The ApplicationProperties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * The I18nBean.
     */
    private final I18nBean.BeanFactory i18n;

    /**
     * The PermissionSchemeManager.
     */
    private final PermissionManager permissionManager;

    /**
     * The WatcherManager instance.
     */
    private final WatcherManager watcherManager;

    /**
     * The UserManager instance.
     */
    private final UserManager userManager;

    /**
     * Creates a new DefaultWatcherService with the given dependencies.
     *
     * @param applicationProperties an ApplicationProperties
     * @param i18n a I18nBean
     * @param permissionManager a PermissionManager
     * @param watcherManager a WatcherManager
     * @param userManager a UserManager
     */
    public DefaultWatcherService(final ApplicationProperties applicationProperties, final I18nHelper.BeanFactory i18n,
            final PermissionManager permissionManager, final WatcherManager watcherManager,
            final UserManager userManager)
    {
        this.watcherManager = watcherManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.i18n = i18n;
    }

    @Override
    public ServiceOutcome<Pair<Integer, List<User>>> getWatchers(final Issue issue, @Nullable final User remoteUser)
            throws WatchingDisabledException
    {
        final Pair<Integer, List<String>> watchers = getWatcherUsernames(issue, remoteUser);

        return ServiceOutcomeImpl.ok(convertUsers(watchers, new UserFromName()));
    }

    @Override
    public ServiceOutcome<List<User>> addWatcher(final Issue issue, final User remoteUser, final User watcher)
            throws WatchingDisabledException
    {
        Pair<Boolean, String> canWatchIssue = canWatchIssue(issue, remoteUser, watcher);
        if (canWatchIssue.first().booleanValue())
        {
            watcherManager.startWatching(watcher, issue);
            return ServiceOutcomeImpl.ok(getCurrentWatchersFor(issue));
        }
        return ServiceOutcomeImpl.error(canWatchIssue.second());
    }

    @Override
    public BulkWatchResult addWatcherToAll(final Collection<Issue> issues, final ApplicationUser remoteUser,
                                           final ApplicationUser watcher) throws WatchingDisabledException
    {
        return addWatcherToAll(issues, remoteUser, watcher, Contexts.nullContext());
    }

    @Override
    public BulkWatchResult addWatcherToAll(final Collection<Issue> issues, final ApplicationUser remoteUser,
            final ApplicationUser watcher, final Context taskContext) throws WatchingDisabledException
    {
        Collection<Issue> successfulIssues = new ArrayList<Issue> ();
        Collection<Pair<Issue,String>> failedIssues = new ArrayList<Pair<Issue,String>> ();

        for (Issue issue : issues)
        {
            Pair<Boolean, String> canWatchIssue = canWatchIssue(issue, remoteUser, watcher);
            if (canWatchIssue.first().booleanValue())
            {
                successfulIssues.add (issue);
            }
            else
            {
                failedIssues.add(Pair.nicePairOf(issue, canWatchIssue.second()));
            }
        }

        if (!successfulIssues.isEmpty())
        {

            watcherManager.startWatching(watcher, successfulIssues, taskContext);
        }

        return new BulkWatchResult(failedIssues);
    }

    @Override
    public ServiceOutcome<List<User>> removeWatcher(final Issue issue, final User remoteUser, final User watcher)
            throws WatchingDisabledException
    {
        Pair<Boolean, String> canUnwatchIssue = canUnwatchIssue(issue, remoteUser, watcher);
        if (canUnwatchIssue.first().booleanValue())
        {
            watcherManager.stopWatching(watcher, issue);
            return ServiceOutcomeImpl.ok(getCurrentWatchersFor(issue));
        }
        return ServiceOutcomeImpl.error(canUnwatchIssue.second());
    }

    @Override
    public BulkWatchResult removeWatcherFromAll(final Collection<Issue> issues, final ApplicationUser remoteUser,
            final ApplicationUser watcher) throws WatchingDisabledException
    {
        return removeWatcherFromAll(issues, remoteUser, watcher, Contexts.nullContext());
    }

    @Override
    public BulkWatchResult removeWatcherFromAll(final Collection<Issue> issues, final ApplicationUser remoteUser,
            final ApplicationUser watcher, final Context taskContext) throws WatchingDisabledException
    {
        Collection<Issue> successfulIssues = new ArrayList<Issue> ();
        Collection<Pair<Issue,String>> failedIssues = new ArrayList<Pair<Issue,String>> ();

        for (Issue issue : issues)
        {
            Pair<Boolean, String> canUnwatchIssue = canUnwatchIssue(issue, remoteUser, watcher);
            if (canUnwatchIssue.first().booleanValue())
            {
                successfulIssues.add (issue);
            }
            else
            {
                failedIssues.add (Pair.nicePairOf(issue, canUnwatchIssue.second()));
            }
        }

        if (!successfulIssues.isEmpty())
        {
            watcherManager.stopWatching(watcher, successfulIssues, taskContext);
        }

        return new BulkWatchResult(failedIssues);
    }

    private Pair<Boolean, String> canWatchIssue (final Issue issue, final User remoteUser, final User watcher)
                throws WatchingDisabledException
    {
        ApplicationUser appRemoteUser = ApplicationUsers.from(remoteUser);
        ApplicationUser appWatcher = ApplicationUsers.from(watcher);

        return canWatchIssue(issue, appRemoteUser, appWatcher);
    }

    private Pair<Boolean, String> canWatchIssue (final Issue issue, final ApplicationUser remoteUser, final ApplicationUser watcher)
            throws WatchingDisabledException
    {
        try
        {
            if (!isWatchingEnabled())
            {
                throw new WatchingDisabledException();
            }
            final boolean canView = permissionManager.hasPermission(Permissions.BROWSE, issue, watcher);
            if (!canView)
            {
                return Pair.nicePairOf(false, buildAddWatcherCannotViewString(issue, remoteUser, watcher));
            }

            checkModifyWatchersPermission(issue, remoteUser, watcher);

            return Pair.nicePairOf(true, null);
        }
        catch (PermissionException e)
        {
            return Pair.nicePairOf(false, buildAddWatcherNotAllowedString(issue, remoteUser));
        }
    }

    private Pair<Boolean, String> canUnwatchIssue (final Issue issue, final User remoteUser, final User watcher)
                throws WatchingDisabledException
    {
        ApplicationUser appRemoteUser = ApplicationUsers.from(remoteUser);
        ApplicationUser appWatcher = ApplicationUsers.from(watcher);

        return canUnwatchIssue(issue, appRemoteUser, appWatcher);
    }

    private Pair<Boolean, String> canUnwatchIssue (final Issue issue, final ApplicationUser remoteUser, final ApplicationUser watcher)
            throws WatchingDisabledException
    {
        try
        {
            if (!isWatchingEnabled())
            {
                throw new WatchingDisabledException();
            }
            checkModifyWatchersPermission(issue, remoteUser, watcher);
            return Pair.nicePairOf(true, null);
        }
        catch (PermissionException e)
        {
            return Pair.nicePairOf(false, buildRemoveUserNotAllowedString(issue, remoteUser));
        }
    }

    @Override
    @Deprecated
    public boolean canWatchAll(final Iterable<Issue> issues, final User remoteUser)
    {
        return canUnwatchAll(issues, ApplicationUsers.from(remoteUser));
    }

    @Override
    public boolean canWatchAll(final Iterable<Issue> issues, final ApplicationUser applicationUser)
    {
        if (!isWatchingEnabled())
        {
            return false;
        }

        for (final Issue issue : issues)
        {
            final boolean canView = permissionManager.hasPermission(Permissions.BROWSE, issue, applicationUser);

            if (!canView)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    @Deprecated
    public boolean canUnwatchAll(final Iterable<Issue> issues, final User remoteUser)
    {
        return isWatchingEnabled();
    }

    @Override
    public boolean canUnwatchAll(final Iterable<Issue> issues, final ApplicationUser remoteUser)
    {
        return isWatchingEnabled();
    }


    /**
     * Returns true iff watching is enabled.
     *
     * @return true iff watching is enabled
     */
    @Override
    public boolean isWatchingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    /**
     * Returns true iff the given User has permission to view the watcher list of the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user can view the watch list
     */
    @Override
    public boolean hasViewWatcherListPermission(final Issue issue, @Nullable final User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, remoteUser) || canEditWatcherList(issue, remoteUser);
    }

    /**
     * Returns a pair containing the watcher count and the watcher usernames for a given issue.
     *
     * @param issue the Issue
     * @param remoteUser the calling User
     * @return a Pair containing the watcher count and the watcher usernames for a given issue
     * @throws WatchingDisabledException if watching is disabled
     */
    protected Pair<Integer, List<String>> getWatcherUsernames(final Issue issue, final User remoteUser)
            throws WatchingDisabledException
    {
        if (!isWatchingEnabled())
        {
            throw new WatchingDisabledException();
        }

        final List<String> watcherNames = watcherManager.getCurrentWatcherUsernames(issue);
        int watcherCount = watcherNames.size();

        // filter out any watchers that the caller is not supposed to see
        if (!hasViewWatcherListPermission(issue, remoteUser))
        {
            if (remoteUser == null)
            {
                watcherNames.clear();
            }
            else
            {
                watcherNames.retainAll(singletonList(remoteUser.getName()));
            }
        }
        LOGGER.trace("Visible watchers on issue '{}': {}", issue.getKey(), watcherNames);

        // always return the actual number of watchers, regardless of permissions. this is necessary to remain
        // consistent with the web UI.
        return Pair.of(watcherCount, watcherNames);
    }

    /**
     * Returns a List containing the users that are currently watching an issue.
     *
     * @param issue the Issue to get the watcher list for
     * @return a List of users that are watching the issue
     */
    protected List<User> getCurrentWatchersFor(final Issue issue)
    {
        final List<String> watcherNames = watcherManager.getCurrentWatcherUsernames(issue);

        return newListFrom(watcherNames, new UserFromName());
    }

    /**
     * Returns true iff the given User has permission to edit the watcher list of the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user can edit the watch list
     */
    protected boolean canEditWatcherList(final Issue issue, @Nullable final User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, remoteUser);
    }

    /**
     * Converts the usernames into User objects using the given function.
     *
     * @param watchers a Pair of watcher count and watcher usernames
     * @param function a Function used for conversion
     * @return a Pair of watcher count and User object
     */
    protected <T extends User> Pair<Integer, List<T>> convertUsers(final Pair<Integer, List<String>> watchers,
            final Function<String, T> function)
    {
        return Pair.<Integer, List<T>>of(
                watchers.first(),
                Lists.newArrayList(Lists.transform(watchers.second(), function))
        );
    }

    /**
     * Ensures that the given remoteUser has permission to add or remove the given watcher to/from the issue. Throws an
     * exception if the user does not have permission.
     *
     * @param issue an Issue
     * @param remoteUser a User representing the caller
     * @param watcher a User representing the watcher to add or remove
     * @throws PermissionException if the caller does not have permission to manage watchers, or cannot see the issue
     * @throws WatchingDisabledException if watching is disabled
     */
    protected void checkModifyWatchersPermission(final Issue issue, final ApplicationUser remoteUser, final ApplicationUser watcher)
            throws PermissionException, WatchingDisabledException
    {
        final boolean canManage = permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, remoteUser);
        if (!(canManage || remoteUser.equals(watcher)))
        {
            throw new PermissionException();
        }
    }

    /**
     * Function object to get a User object from the user name.
     */
    class UserFromName implements Function<String, User>
    {
        public User apply(final String username)
        {
            return userManager.getUserEvenWhenUnknown(username);
        }
    }

    /**
     * Thrown if a user does not have permission to manage watchers.
     */
    static class PermissionException extends Exception
    {
        // empty
    }

    /**
     * Returns a localised error message indicating that the caller is not allowed to add a watcher to the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a String containing an error message
     */
    private String buildAddWatcherNotAllowedString(final Issue issue, final ApplicationUser remoteUser)
    {
        return i18n.getInstance(remoteUser).getText("watcher.service.error.add.watcher.not.allowed", remoteUser.getName(), issue.getKey());
    }

    /**
     * Returns a localised error message indicating that the proposed watcher canot view the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @param watcher a User
     * @return a String containing an error message
     */
    private String buildAddWatcherCannotViewString(final Issue issue, final ApplicationUser remoteUser, final ApplicationUser watcher)
    {
        return i18n.getInstance(remoteUser).getText("watcher.error.user.cant.see.issue", watcher.getName(), issue.getKey());
    }

    /**
     * Returns a localised error message indicating that the caller is not allowed to remove a watcher from the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a String containing an error message
     */
    private String buildRemoveUserNotAllowedString(final Issue issue, final ApplicationUser remoteUser)
    {
        return i18n.getInstance(remoteUser).getText("watcher.service.error.remove.watcher.not.allowed", remoteUser.getName(), issue.getKey());
    }

    /**
     * Creates a new List from another using a Function.
     *
     * @param from the from list
     * @param fn the function
     * @param <F> the from type
     * @param <T> the to type
     * @return a new List
     */
    static <F, T> List<T> newListFrom(final List<F> from, final Function<F, T> fn)
    {
        return Lists.newArrayList(Lists.transform(from, fn));
    }
}
