package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.event.issue.IssueWatcherAddedEvent;
import com.atlassian.jira.event.issue.IssueWatcherDeletedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.ApplicationUserBestNameComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultWatcherManager implements WatcherManager
{
    private static final Logger log = Logger.getLogger(DefaultWatcherManager.class);
    public static final String ASSOCIATION_TYPE = "WatchIssue";

    private final UserAssociationStore userAssociationStore;
    private final ApplicationProperties applicationProperties;
    private final IssueIndexManager indexManager;
    private final UserManager userManager;
    private final IssueFactory issueFactory;
    private final EventPublisher eventPublisher;
    private final IssueManager issueManager;

    public DefaultWatcherManager(final UserAssociationStore userAssociationStore, final ApplicationProperties applicationProperties,
            final IssueIndexManager indexManager, final UserManager userManager, IssueFactory issueFactory, EventPublisher eventPublisher, IssueManager issueManager)
    {
        this.userAssociationStore = userAssociationStore;
        this.applicationProperties = applicationProperties;
        this.indexManager = indexManager;
        this.userManager = userManager;
        this.issueFactory = issueFactory;
        this.eventPublisher = eventPublisher;
        this.issueManager = issueManager;
    }

    public void startWatching(final User user, final Issue issue)
    {
        updateWatch(true, ApplicationUsers.from(user), issue);
    }

    @Override
    public void startWatching(ApplicationUser user, Issue issue)
    {
        updateWatch(true, user, issue);
    }

    @Override
    public void startWatching(ApplicationUser user, Collection<Issue> issues)
    {
        startWatching(user, issues, Contexts.nullContext());
    }

    @Override
    public void startWatching(ApplicationUser user, Collection<Issue> issues, Context taskContext)
    {
        for (Issue issue : issues)
        {
            Context.Task task = taskContext.start(issue);
            updateWatch(true, user, issue, false);
            task.complete();
        }
        reindex(issues);
    }

    public void startWatching(final User user, final GenericValue issueGV)
    {
        updateWatch(true, ApplicationUsers.from(user), issueFactory.getIssueOrNull(issueGV));
    }

    public void stopWatching(final String username, final GenericValue issueGV)
    {
        updateWatch(false, userManager.getUserByNameEvenWhenUnknown(username), issueFactory.getIssueOrNull(issueGV));
    }

    public void stopWatching(final User user, final Issue issue)
    {
        updateWatch(false, userManager.getUserByNameEvenWhenUnknown(user.getName()), issue);
    }

    @Override
    public void stopWatching(ApplicationUser user, Issue issue)
    {
        updateWatch(false, user, issue);
    }

    @Override
    public void stopWatching(ApplicationUser user, Collection<Issue> issues)
    {
        stopWatching(user, issues, Contexts.nullContext());
    }

    @Override
    public void stopWatching(ApplicationUser user, Collection<Issue> issues, final Context taskContext)
    {
        for (Issue issue : issues)
        {
            Context.Task task = taskContext.start(issue);
            updateWatch(false, userManager.getUserByNameEvenWhenUnknown(user.getName()), issue, false);
            task.complete();
        }
        reindex(issues);
    }

    public void stopWatching(final User user, final GenericValue issueGV)
    {
        updateWatch(false, ApplicationUsers.from(user), issueFactory.getIssueOrNull(issueGV));
    }

    public List<String> getCurrentWatcherUsernames(final Issue issue) throws DataAccessException
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue.getGenericValue());
    }

    public boolean isWatchingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    public boolean isWatching(final User user, final Issue issue)
    {
        return isWatching(ApplicationUsers.from(user), issue);
    }

    @Override
    public boolean isWatching(ApplicationUser user, Issue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no watches for the issue then this dude isn't watching it.
        Long watches = issue.getWatches();
        if (watches == null || watches == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, Entity.Name.ISSUE, issue.getId());
    }

    // Determine whether the current user is already watching the issue or not
    public boolean isWatching(final User user, final GenericValue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no watches for the issue then this dude isn't watching it.
        final Long watches = issue.getLong("watches");
        if (watches == null || watches == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, Entity.Name.ISSUE, issue.getLong("id"));
    }

    public Collection<User> getCurrentWatchList(final Issue issue, final Locale userLocale)
    {
        return ApplicationUsers.toDirectoryUsers(getWatchers(issue, userLocale));
    }

    @Override
    public List<ApplicationUser> getWatchers(Issue issue, Locale userLocale)
    {
        final List<ApplicationUser> watchers = userAssociationStore.getUsersFromSink(ASSOCIATION_TYPE, issue.getGenericValue());
        Collections.sort(watchers, new ApplicationUserBestNameComparator(userLocale));
        return watchers;
    }

    @Override
    public int getWatcherCount(Issue issue)
    {
        return userAssociationStore.getUserkeysFromIssue(ASSOCIATION_TYPE, issue.getId()).size();
    }

    @Override
    public Collection<String> getWatcherUserKeys(Issue issue)
    {
        return userAssociationStore.getUserkeysFromIssue(ASSOCIATION_TYPE, issue.getId());
    }

    public List<String> getCurrentWatcherUsernames(final GenericValue issue) throws DataAccessException
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue);
    }

    private boolean updateWatch(final boolean addWatch, final ApplicationUser user, final Issue issue)
    {
        return updateWatch(addWatch, user, issue, true);
    }

    private boolean updateWatch(final boolean addWatch, final ApplicationUser user, final Issue issue, boolean reindex)
    {
        if (validateUpdate(user, issue))
        {
            try
            {
                if (addWatch)
                {
                    if (!isWatching(user, issue))
                    {
                        userAssociationStore.createAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustWatchCount(issue.getGenericValue(), 1, reindex);
                        eventPublisher.publish(new IssueWatcherAddedEvent(issue, user));
                        return true;
                    }
                }
                else
                {
                    if (isWatching(user, issue))
                    {
                        userAssociationStore.removeAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustWatchCount(issue.getGenericValue(), -1, reindex);
                        eventPublisher.publish(new IssueWatcherDeletedEvent(issue, user));
                        return true;
                    }
                }
            }
            catch (final GenericEntityException e)
            {
                log.error("Error changing watch association", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Validates that the params andd the system are in a correct state to change a watch
     *
     * @param user  The user who is watching
     * @param issue the issue the user is voting for
     * @return whether or not to go ahead with the watch.
     */
    private boolean validateUpdate(final ApplicationUser user, final Issue issue)
    {
        if (issue == null)
        {
            log.error("You must specify an issue.");
            return false;
        }

        if (user == null)
        {
            log.error("You must specify a user.");
            return false;
        }
        return true;
    }

    /**
     * Adjusts the watch count for an issue.
     *
     * @param originalIssue       the issue to change count for
     * @param adjustValue the value to change it by
     * @param reindex     a boolean specifying if the function should reindex the issue after adjusting the count
     * @throws GenericEntityException If there wasa persitence problem
     */

    private void adjustWatchCount(final GenericValue originalIssue, final int adjustValue, final boolean reindex) throws GenericEntityException
    {
        final long watches = recalculateWatches(originalIssue, adjustValue);
        final Long issueId = originalIssue.getLong("id");

        final GenericValue clonedIssue = (GenericValue) originalIssue.clone();
        clonedIssue.clear();
        clonedIssue.set("id", issueId);
        clonedIssue.set("watches", watches);
        clonedIssue.store();

        // The issue reference is kept in the ManageWatchers action, need to update original issue too
        originalIssue.set("watches", watches);

        if (reindex)
        {
            // indexing doesn't handles incremental updates, re-indexing the updated object from db
            final GenericValue updatedIssue = issueManager.getIssue(issueId);
            reindex(updatedIssue);
        }
    }

    private long recalculateWatches(final GenericValue issue, final int adjustValue)
    {
        Long watches = issue.getLong("watches");
        if (watches == null)
        {
            watches = 0L;
        }
        watches = watches + adjustValue;

        if (watches < 0)
        {
            watches = 0L;
        }
        return watches;
    }

    @Override
    public void removeAllWatchesForUser(final User user)
    {
        notNull("User", user);
        removeAllWatchesForUser(ApplicationUsers.from(user));
    }

    @Override
    public void removeAllWatchesForUser(final ApplicationUser user)
    {
        notNull("User", user);
        // Find the Issues that this User watches - we need to reindex them later
        final List<GenericValue> watchedIssues = userAssociationStore.getSinksFromUser(ASSOCIATION_TYPE, user, "Issue");

        userAssociationStore.removeUserAssociationsFromUser(ASSOCIATION_TYPE, user, "Issue");

        for (final GenericValue issue : watchedIssues)
        {
            reindex(issue);
            eventPublisher.publish(new IssueWatcherDeletedEvent(issueFactory.getIssue(issue), user));
        }
    }

    private void reindex(final GenericValue issue)
    {
        try
        {
            indexManager.reIndex(issueFactory.getIssue(issue), false, false);
        }
        catch (final IndexException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void reindex(final Collection<Issue> issues)
    {
        try
        {
            indexManager.reIndexIssueObjects(issues);
        }
        catch (final IndexException e)
        {
            throw new RuntimeException(e);
        }
    }
}
