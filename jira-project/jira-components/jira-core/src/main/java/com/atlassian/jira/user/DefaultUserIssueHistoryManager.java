package com.atlassian.jira.user;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A convenience wrapper around the {@link com.atlassian.jira.user.UserHistoryManager} to work directly with issues and
 * perform permission checks
 *
 * @since v4.0
 */
public class DefaultUserIssueHistoryManager implements UserIssueHistoryManager
{
    private static final Logger log = Logger.getLogger(DefaultUserIssueHistoryManager.class);

    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final UserHistoryManager userHistoryManager;
    private final ApplicationProperties applicationProperties;

    public DefaultUserIssueHistoryManager(final UserHistoryManager userHistoryManager, final PermissionManager permissionManager, final IssueManager issueManager, final ApplicationProperties applicationProperties)
    {
        this.userHistoryManager = userHistoryManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
    }

    public void addIssueToHistory(@Nonnull final User user, @Nonnull final Issue issue)
    {
        addIssueToHistory(ApplicationUsers.from(user), issue);
    }

    public void addIssueToHistory(@Nonnull final ApplicationUser user, @Nonnull final Issue issue)
    {
        notNull("issue", issue);
        userHistoryManager.addItemToHistory(UserHistoryItem.ISSUE, user, issue.getId().toString());
    }

    public boolean hasIssueHistory(final User user)
    {
        return hasIssueHistory(ApplicationUsers.from(user));
    }

    public boolean hasIssueHistory(final ApplicationUser user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.ISSUE, user);

        // paranoia is good
        //noinspection ConstantConditions
        if (history != null)
        {
            for (final UserHistoryItem historyItem : history)
            {
                final Issue issue = issueManager.getIssueObject(Long.valueOf(historyItem.getEntityId()));
                if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    public List<UserHistoryItem> getFullIssueHistoryWithoutPermissionChecks(final User user)
    {
        return getFullIssueHistoryWithoutPermissionChecks(ApplicationUsers.from(user));
    }

    @Nonnull
    public List<UserHistoryItem> getFullIssueHistoryWithoutPermissionChecks(final ApplicationUser user)
    {
        return userHistoryManager.getHistory(UserHistoryItem.ISSUE, user);
    }

    @Nonnull
    public List<UserHistoryItem> getFullIssueHistoryWithPermissionChecks(final User user)
    {
        return getFullIssueHistoryWithPermissionChecks(ApplicationUsers.from(user));
    }

    @Nonnull
    public List<UserHistoryItem> getFullIssueHistoryWithPermissionChecks(final ApplicationUser user)
    {
        return getViewableIssueHistory(user, Integer.MAX_VALUE);
    }

    @Nonnull
    public List<Issue> getShortIssueHistory(final User user)
    {
        return getShortIssueHistory(ApplicationUsers.from(user));
    }

    @Nonnull
    public List<Issue> getShortIssueHistory(final ApplicationUser user)
    {
        int maxItems = UserIssueHistoryManager.DEFAULT_ISSUE_HISTORY_DROPDOWN_ITEMS;
        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS));
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.dropdown.items'.  Should be a number.");
        }
        return getViewableIssuesFromHistory(user, maxItems);
    }

    /**
     * @param user     the user to retrieve the history of
     * @param maxItems the maximum number of items to return; use {@code Integer.MAX_VALUE} for no limit
     * @return the user history containing only the issues which the user has permission to view, limited maxItems if specified
     */
    private List<Issue> getViewableIssuesFromHistory(final ApplicationUser user, final int maxItems)
    {
        final List<UserHistoryItem> history = getFullIssueHistoryWithoutPermissionChecks(user);

        // Paranoid, but ok
        //noinspection ConstantConditions
        if (history == null)
        {
            return Lists.newArrayList();
        }

        final List<Issue> returnList = new ArrayList<Issue>(history.size());
        for (final UserHistoryItem userHistoryItem : history)
        {
            final Issue issue = issueManager.getIssueObject(Long.valueOf(userHistoryItem.getEntityId()));
            if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
            {
                returnList.add(issue);
                if (returnList.size() >= maxItems)
                {
                    return returnList;
                }
            }
        }
        return returnList;
    }

    /**
     * @param user     the user to retrieve the history of
     * @param maxItems the maximum number of items to return; use {@code Integer.MAX_VALUE} for no limit
     * @return the user history containing only the issues which the user has permission to view, limited maxItems if specified
     */
    private List<UserHistoryItem> getViewableIssueHistory(final ApplicationUser user, final int maxItems)
    {
        final List<UserHistoryItem> history = getFullIssueHistoryWithoutPermissionChecks(user);

        // Paranoid, but ok
        //noinspection ConstantConditions
        if (history == null)
        {
            return Lists.newArrayList();
        }

        final List<UserHistoryItem> returnList = new ArrayList<UserHistoryItem>(history.size());
        for (final UserHistoryItem userHistoryItem : history)
        {
            final Issue issue = issueManager.getIssueObject(Long.valueOf(userHistoryItem.getEntityId()));
            if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
            {
                returnList.add(userHistoryItem);
                if (returnList.size() >= maxItems)
                {
                    return returnList;
                }
            }
        }
        return returnList;
    }
}
