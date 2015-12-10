package com.atlassian.jira.bc.user.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.UserSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.lang.StringUtils;

/**
 * The main implementation of {@link AssigneeService}.
 *
 * @since v5.0
 */
public class DefaultAssigneeService implements AssigneeService
{
    private final PermissionContextFactory permissionContextFactory;
    private final PermissionSchemeManager permissionSchemeManager;
    private final UserHistoryManager userHistoryManager;
    private ChangeHistoryManager changeHistoryManager;
    private final FeatureManager featureManager;
    private final JiraAuthenticationContext authenticationContext;
    private final UserKeyService userKeyService;

    public DefaultAssigneeService(PermissionContextFactory permissionContextFactory,
            PermissionSchemeManager permissionSchemeManager, UserHistoryManager userHistoryManager,
            FeatureManager featureManager, final JiraAuthenticationContext authenticationContext, UserKeyService userKeyService) {
        this.permissionContextFactory = permissionContextFactory;
        this.permissionSchemeManager = permissionSchemeManager;
        this.userHistoryManager = userHistoryManager;
        this.featureManager = featureManager;
        this.authenticationContext = authenticationContext;
        this.userKeyService = userKeyService;
    }

    @Override
    public List<User> getSuggestedAssignees(Issue issue, @Nullable User loggedInUser, @Nullable ActionDescriptor actionDescriptor)
    {
        List<User> assignableUsers = new AssignableUsers(issue, actionDescriptor).findAll();
        List<User> suggestedAssignees = getSuggestedAssignees(issue, loggedInUser, assignableUsers);

        Collections.sort(suggestedAssignees, new UserCachingComparator(authenticationContext.getLocale()));
        return suggestedAssignees;
    }

    @Override
    public List<User> getSuggestedAssignees(Issue issue, User loggedInUser, List<User> assignableUsers)
    {
        Set<String> suggestedAssigneeKeys = getSuggestedAssigneeKeys(issue, loggedInUser);
        return getSuggestedAssigneesFromKeys(suggestedAssigneeKeys, assignableUsers);
    }

    @Override
    public Collection<User> findAssignableUsers(String query, Issue issue, @Nullable ActionDescriptor actionDescriptor)
    {
        return new AssignableUsers(issue, actionDescriptor).matchingUsername(query).findAllAndSort();
    }

    @Override
    public Collection<User> findAssignableUsers(String query, Project project)
    {
        return new AssignableUsers(project).matchingUsername(query).findAllAndSort();
    }

    private Set<User> findAssignableUsers(final String query, final Set<User> assignableUsers)
    {
        final String convertedQuery = (query == null) ? "" : query.trim().toLowerCase();
        if (StringUtils.isBlank(query))
        {
            return assignableUsers;
        }

        final Set<User> returnUsers = new HashSet<User>();
        final Predicate<User> userMatcher = new UserMatcherPredicate(convertedQuery, true);
        for (final User user : assignableUsers)
        {
            if (userMatcher.apply(user))
            {
                returnUsers.add(user);
            }
        }
        return returnUsers;
    }

    private Set<String> getSuggestedAssigneeKeys(Issue issue, final User loggedInUser)
    {
        Set<String> suggestedAssignees = new HashSet<String>();

        if (loggedInUser != null)
        {
            // HACK - temp only to make old tests pass - only add the logged in user in Frother mode
            if (useFrotherControl())
            {
                // We need the key, not the username
                suggestedAssignees.add(userKeyService.getKeyForUser(loggedInUser));
            }
        }
        suggestedAssignees.addAll(getRecentAssigneeKeysForIssue(issue));
        suggestedAssignees.addAll(getRecentAssigneeKeysForUser(loggedInUser));

        // Reporter may be null for new issues.
        User reporter = issue.getReporter();
        if (reporter != null)
        {
            // We need the key, not the username
            suggestedAssignees.add(userKeyService.getKeyForUser(reporter));
        }

        return suggestedAssignees;
    }

    private boolean useFrotherControl()
    {
        // The Frother Assignee field breaks some old tests expecting the select element to exist with all user
        // options - allow these tests to run without it in the short term by setting the 'off' flag.
        boolean on = featureManager.isEnabled("frother.assignee.field");
        boolean off = featureManager.isEnabled("no.frother.assignee.field");
        return on && !off;
    }

    private List<User> getSuggestedAssigneesFromKeys(final Set<String> suggestedAssigneeKeys, List<User> assignableUsers)
    {
        List<User> suggestedAssignees = new ArrayList<User>();

        if (!suggestedAssigneeKeys.isEmpty())
        {
            for (User user : assignableUsers)
            {
                // Whittle away at the suggest collection so that each run of the (potentially-long) loop goes a little faster
                final String key = userKeyService.getKeyForUser(user);
                if (suggestedAssigneeKeys.remove(key))
                {
                    suggestedAssignees.add(user);
                }
            }
        }

        return suggestedAssignees;
    }

    /**
     * Given a set of suggested names and an ordered list of assignable users, returns an order list of suggested users.
     */
    public List<User> getSuggestedAssignees(final Set<String> suggestedAssigneeNames, List<User> assignableUsers)
    {
        List<User> suggestedAssignees = new ArrayList<User>();

        if (!suggestedAssigneeNames.isEmpty())
        {
            for (User user : assignableUsers)
            {
                // Whittle away at the suggest collection so that each run of the (potentially-long) loop goes a little faster
                if (suggestedAssigneeNames.remove(user.getName()))
                {
                    suggestedAssignees.add(user);
                }
            }
        }

        return suggestedAssignees;
    }

    public List<User> getAssignableUsers(Issue issue, ActionDescriptor actionDescriptor)
    {
        return new AssignableUsers(issue, actionDescriptor).findAllAndSort();
    }

    public List<User> getAssignableUsers(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        // Special cases
        if (issues == null)
        {
            return Collections.emptyList();
        }

        final Iterator<Issue> iter = issues.iterator();
        if (!iter.hasNext())
        {
            return Collections.emptyList();
        }

        // Start out will all the assignable users from the first issue
        final Set<User> assignableUsers = new AssignableUsers(iter.next(), actionDescriptor).findAllAsSet();

        // Keep filtering until we either run out of issues or exclude all of the users
        while (iter.hasNext() && !assignableUsers.isEmpty())
        {
            assignableUsers.retainAll(new AssignableUsers(iter.next(), actionDescriptor).findAllAsSet());
        }

        final List<User> sortedUsers = new ArrayList<User>(assignableUsers);
        Collections.sort(sortedUsers, new UserCachingComparator(authenticationContext.getLocale()));
        return sortedUsers;
    }

    /**
     * Gets ids of this issue's recent assignees, including the current assignee.
     *
     * @param issue an issue to get the change history of
     * @return a set of ids
     */
    @Override
    public Set<String> getRecentAssigneeKeysForIssue(Issue issue)
    {
        Set<String> recentAssignees = new HashSet<String>();

        List<ChangeItemBean> assigneeHistory = getChangeHistoryManager().getChangeItemsForField(issue, "assignee");

        // Sort by descending date - for the most recent assignees
        Collections.sort(assigneeHistory, new Comparator<ChangeItemBean>()
        {
            @Override
            public int compare(ChangeItemBean changeItemBean1, ChangeItemBean changeItemBean2)
            {
                return changeItemBean2.getCreated().compareTo(changeItemBean1.getCreated());
            }
        });
        for (ChangeItemBean changeItemBean : assigneeHistory)
        {
            // Could be reverse-sorted on date assigned and return a list?
            recentAssignees.add(changeItemBean.getTo());
            if (recentAssignees.size() >= 5)
            {
                break;
            }
        }

        // Assignee may be null for new issues.
        String assigneeId = issue.getAssigneeId();
        if (assigneeId != null)
        {
            recentAssignees.add(assigneeId);
        }

        return recentAssignees;
    }

    /**
     * Gets the names of this issue's recent assignees, including the current assignee.
     *
     * @param issue an issue to get the change history of
     * @return a set of usernames
     */
    @Override
    public Set<String> getRecentAssigneeNamesForIssue(Issue issue)
    {
        final Set<String> results = Sets.newHashSet();

        for (String key : getRecentAssigneeKeysForIssue(issue))
        {
            final String username = userKeyService.getUsernameForKey(key);
            if (username != null)
            {
                results.add(username);
            }
        }

        return results;
    }



    // JRA-14128: make a map of the counts of the Full Names of the users,
    // so that we can detect which users have duplicate Full Names
    public Map<String, Boolean> makeUniqueFullNamesMap(Collection<User> users)
    {
        Map<String, Boolean> uniqueFullNames = new HashMap<String, Boolean>();

        for (User user : users)
        {
            String fullName = user.getDisplayName();
            Boolean isUnique = uniqueFullNames.get(fullName);
            if (isUnique == null)
            {
                uniqueFullNames.put(fullName, Boolean.TRUE);
            }
            else
            {
                uniqueFullNames.put(fullName, Boolean.FALSE);
            }
        }
        return uniqueFullNames;
    }

    /**
     * Get users names that the given user has recently assigned issues to.
     *
     * @param remoteUser a User
     * @return a list of user names
     */
    @Override
    public Set<String> getRecentAssigneeNamesForUser(User remoteUser)
    {
        final Set<String> results = Sets.newHashSet();

        for (String key : getRecentAssigneeKeysForUser(remoteUser))
        {
            final String userName = userKeyService.getUsernameForKey(key);
            if (userName != null)
            {
                results.add(userName);
            }
        }

        return results;
    }

    /**
     * Get users keys that the given user has recently assigned issues to.
     *
     * @param remoteUser a User
     * @return a list of user keys
     */
    @Override
    public Set<String> getRecentAssigneeKeysForUser(User remoteUser)
    {
        List<UserHistoryItem> recentUserHistory = new ArrayList<UserHistoryItem>(userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, remoteUser));

        // Sort user history in descending date order
        Collections.sort(recentUserHistory, new Comparator<UserHistoryItem>()
        {
            @Override
            public int compare(UserHistoryItem userHistoryItem1, UserHistoryItem userHistoryItem2)
            {
                return (int)(userHistoryItem2.getLastViewed() - userHistoryItem1.getLastViewed());
            }
        });
        Set<String> recentHistoryAssignees = new HashSet<String>();

        for (UserHistoryItem userHistoryItem : recentUserHistory)
        {
            recentHistoryAssignees.add(userHistoryItem.getEntityId());
            if (recentHistoryAssignees.size() >= 5)
            {
                break;
            }
        }
        return recentHistoryAssignees;
    }

    void setChangeHistoryManager(ChangeHistoryManager changeHistoryManager)
    {
        this.changeHistoryManager = changeHistoryManager;
    }

    private ChangeHistoryManager getChangeHistoryManager()
    {
        if (changeHistoryManager == null)
        {
            setChangeHistoryManager(ComponentAccessor.getChangeHistoryManager());
        }
        return changeHistoryManager;
    }

    /**
     * An "assignable users" query. The results can be obtained in sorted or unsorted form.
     */
    final class AssignableUsers
    {
        private final Issue issue;
        private final PermissionContext ctx;
        private final String matchingUsername;

        AssignableUsers(Project project)
        {
            this.issue = null;
            this.ctx = permissionContextFactory.getPermissionContext(project);
            this.matchingUsername = null;
        }

        AssignableUsers(Issue issue, ActionDescriptor actionDescriptor)
        {
            this.issue = issue;
            this.ctx = permissionContextFactory.getPermissionContext(issue, actionDescriptor);
            this.matchingUsername = null;
        }

        AssignableUsers(Issue issue, PermissionContext ctx, String matchingUsername)
        {
            this.issue = issue;
            this.ctx = ctx;
            this.matchingUsername = matchingUsername;
        }

        public AssignableUsers matchingUsername(String matchingUsername)
        {
            return new AssignableUsers(issue, ctx, matchingUsername);
        }

        public List<User> findAll()
        {
            return Lists.newArrayList(findAllAsSet());
        }

        Set<User> findAllAsSet()
        {
            UserSet userSet = new UserSet(permissionSchemeManager.getUsers((long) Permissions.ASSIGNABLE_USER, ctx));

            // Current assignee can always stay assigned , even if they are now inactive or no longer in the right group
            if (issue != null && issue.getAssignee() != null)
            {
                userSet.add(issue.getAssignee());
            }

            Set<User> assignableUsers = userSet.toSet();
            // Optionally filter by the username query
            if (matchingUsername != null)
            {
                assignableUsers = findAssignableUsers(matchingUsername, assignableUsers);
            }
            return assignableUsers;
        }


        public List<User> findAllAndSort()
        {
            List<User> users = findAll();

            // Sort on Full Name (the compareTo in User is on username so we need our own Comparator)
            Collections.sort(users, new UserCachingComparator(authenticationContext.getLocale()));
            return users;
        }
    }
}
