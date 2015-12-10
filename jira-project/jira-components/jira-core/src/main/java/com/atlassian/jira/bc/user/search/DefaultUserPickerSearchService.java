package com.atlassian.jira.bc.user.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.StopWatch;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class DefaultUserPickerSearchService implements UserPickerSearchService
{
    private static final Logger log = Logger.getLogger(DefaultUserPickerSearchService.class);

    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;

    /**
     * Constructs a DefaultUserPickerSearchService
     *
     * @param userManager              the UserUtil needed
     * @param applicationProperties the ApplicationProperties
     * @param authenticationContext
     * @param permissionManager     needed to resolve permissions
     * @param projectManager
     * @param projectRoleManager
     */
    public DefaultUserPickerSearchService(final UserManager userManager, final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final GroupManager groupManager, final ProjectManager projectManager,
            final ProjectRoleManager projectRoleManager)
    {
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.groupManager = groupManager;
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public List<User> findUsers(final JiraServiceContext jiraServiceContext, final String query)
    {
        if (StringUtils.isBlank(query))
        {
            return Collections.emptyList();
        }

        return findUsers(jiraServiceContext, query, UserSearchParams.ACTIVE_USERS_IGNORE_EMPTY_QUERY);
    }

    @Override
    public User getUserByName(JiraServiceContext jiraServiceContext, String query)
    {
        return userManager.getUser(query);
    }

    @Override
    public List<User> findUsersAllowEmptyQuery(final JiraServiceContext jiraServiceContext, final String query)
    {
        return findUsers(jiraServiceContext, query, UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY);
    }

    @Override
    public List<User> findUsers(JiraServiceContext jiraServiceContext, String query, UserSearchParams userSearchParams)
    {
        // is it allowed?  How did they get here anyway??
        if (!canPerformAjaxSearch(jiraServiceContext))
        {
            return Collections.emptyList();
        }

        return findUsers(query, UserSearchParams.builder(userSearchParams)
                                    .canMatchEmail(canShowEmailAddresses(jiraServiceContext)).build());
    }

    @Override
    public List<User> findUsers(final String query, final UserSearchParams userSearchParams)
    {
        return findUsers(query, null, userSearchParams);
    }

    @Override
    public List<User> findUsers(final String nameQuery, final String emailQuery, final UserSearchParams userSearchParams)
    {
        // Allow empty queries?
        if (areQueriesNotAllowed(nameQuery, emailQuery, userSearchParams))
        {
            return Collections.emptyList();
        }

        StopWatch stopWatch = new StopWatch();
        final String convertedQuery = convertQuery(nameQuery);
        final String convertedEmailQuery = convertQuery(emailQuery);
        if (log.isDebugEnabled())
            log.debug("Running user-picker search: '" + convertedQuery + "', emailQuery '" + convertedEmailQuery + "'");
        List<User> returnUsers = new ArrayList<User>();

        // search using additional parameters in userFilter
        Collection<User> allUsers = getUsersByUserFilter(userSearchParams.getUserFilter(), userSearchParams.getProjectIds());
        if (allUsers == null)
        {
            // userFilter is disabled, resorting to all users :|
            allUsers = userManager.getUsers();
        }
        if (log.isDebugEnabled())
            log.debug("Found all " + allUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");

        final Predicate<User> userMatcher = new UserMatcherPredicate(convertedQuery, convertedEmailQuery, userSearchParams.canMatchEmail());
        for (final User user : allUsers)
        {
            if (userMatchesQueries(user, userSearchParams, userMatcher))
            {
                returnUsers.add(user);
            }
        }
        if (log.isDebugEnabled())
            log.debug("Matched " + returnUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");
        Collections.sort(returnUsers, new UserCachingComparator(authenticationContext.getLocale()));
        if (log.isDebugEnabled())
        {
            log.debug("Sorted top " + returnUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");
            log.debug("User-picker search completed in " + stopWatch.getTotalTime() + "ms");
        }
        return returnUsers;
    }

    private Collection<User> getUsersByUserFilter(final UserFilter userFilter, Set<Long> projectIds)
    {
        if (userFilter == null || !userFilter.isEnabled())
        {
            return null;
        }

        Collection<User> allUsers = getUsersByGroups(userFilter.getGroups(), null);
        allUsers = getUsersByRoles(userFilter.getRoleIds(), projectIds, allUsers);

        return allUsers == null ? Lists.<User>newArrayList() : allUsers;
    }

    private Collection<User> getUsersByRoles(final Set<Long> roleIds, final Set<Long> projectIds, final Collection<User> existingUsers)
    {
        if (CollectionUtils.isEmpty(projectIds) || CollectionUtils.isEmpty(roleIds))
        {
            return existingUsers;
        }

        // only search by roles if projectIds is not empty
        // Note that projectIds list should have been at least populated with the list of browsable projects by the current user
        // create the set to inform following codes that it's not search all
        Collection<User> allUsers = existingUsers == null ? Sets.<User>newHashSet() : existingUsers;

        for (Project project : getProjects(projectIds))
        {
            for (long roleId : roleIds)
            {
                // ok to repeat calls to projectRoleManager, as it has cache
                ProjectRole projectRole = projectRoleManager.getProjectRole(roleId);
                if (projectRole != null)
                {
                    allUsers.addAll(projectRoleManager.getProjectRoleActors(projectRole, project).getUsers());
                }
            }
        }
        return allUsers;
    }

    private Collection<User> getUsersByGroups(final Set<String> groupNames, final Collection<User> existingUsers)
    {
        if (CollectionUtils.isEmpty(groupNames))
        {
            return existingUsers;
        }
        // create the set to inform following codes that we have performed a search
        Collection<User> allUsers = existingUsers == null ? Sets.<User>newHashSet() : existingUsers;
        // retrieve users in the groups, instead of getting all users
        for (String groupName : groupNames)
        {
            allUsers.addAll(groupManager.getUsersInGroup(groupName));
        }

        return allUsers;
    }

    private boolean userMatches(final ApplicationUser user, final String nameQuery, final String emailQuery, final UserSearchParams userSearchParams)
    {
        if (user == null)
        {
            return false;
        }

        // Allow empty queries?
        if (areQueriesNotAllowed(nameQuery, emailQuery, userSearchParams))
        {
            return false;
        }

        // check whether the user matches the queries
        final Predicate<User> userMatcher = new UserMatcherPredicate(convertQuery(nameQuery), convertQuery(emailQuery), userSearchParams.canMatchEmail());
        if (!userMatchesQueries(user.getDirectoryUser(), userSearchParams, userMatcher))
        {
            return false;
        }

        // check using additional parameters in userFilter
        return userMatchesByUserFilter(user, userSearchParams.getUserFilter(), userSearchParams.getProjectIds());
    }

    @Override
    public boolean userMatches(final ApplicationUser user, final UserSearchParams userSearchParams)
    {
        final UserSearchParams allowEmptyQueryParams = userSearchParams.allowEmptyQuery() ? userSearchParams :
                UserSearchParams.builder(userSearchParams).allowEmptyQuery(true).build();
        return userMatches(user, null, null, allowEmptyQueryParams);
    }

    private boolean userMatchesByUserFilter(final ApplicationUser user, final UserFilter userFilter, final Set<Long> projectIds)
    {
        return userFilter == null || !userFilter.isEnabled() // return true if userFilter is disabled
                || userMatchesByGroups(user, userFilter.getGroups())
                || userMatchesByRoles(user, userFilter.getRoleIds(), projectIds);
    }

    private boolean userMatchesByRoles(final ApplicationUser user, final Set<Long> roleIds, final Set<Long> projectIds)
    {
        if (CollectionUtils.isEmpty(roleIds))
        {
            return false;
        }

        for (Project project : getProjects(projectIds))
        {
            for (long roleId : roleIds)
            {
                // ok to repeat calls to projectRoleManager, as it has cache
                ProjectRole projectRole = projectRoleManager.getProjectRole(roleId);
                if (projectRole != null)
                {
                    if (projectRoleManager.isUserInProjectRole(user, projectRole, project))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Retrieve the list of project objects based on a set of project id's with some smartness
     * to avoid too many db round trips.
     * @param projectIds the ids of the projects to be retrieved
     * @return the project objects
     */
    private Collection<Project> getProjects(final Set<Long> projectIds)
    {
        if (CollectionUtils.isEmpty(projectIds))
        {
            // when used in a context where specific projects could be found, the caller is responsible for
            // making sure that all browsable project ids by the current user is passed in as <code>projectIds</code>
            // because only the caller knows the calling user.
            return ImmutableList.of();
        }
        else if (projectIds.size() == 1)
        {
            Project project = projectManager.getProjectObj(projectIds.iterator().next());
            return project == null ? ImmutableList.<Project>of() : ImmutableList.of(project);
        }
        else
        {
            // try to retrieve all projects at one go, and return those whose ID is in the set
            final List<Project> projects = projectManager.getProjectObjects();
            return Collections2.filter(projects, new Predicate<Project>()
            {
                @Override
                public boolean apply(@Nullable final Project input)
                {
                    return input != null && projectIds.contains(input.getId());
                }
            });
        }
    }

    private boolean userMatchesByGroups(final ApplicationUser user, final Set<String> groupNames)
    {
        if (CollectionUtils.isEmpty(groupNames))
        {
            return false;
        }
        Collection<String> groupsUser = groupManager.getGroupNamesForUser(user.getDirectoryUser());
        for (String groupUser : groupsUser)
        {
            if (groupNames.contains(groupUser))
            {
                return true;
            }
        }

        return false;
    }

    private String convertQuery(final String nameQuery)
    {
        return (nameQuery == null) ? "" : nameQuery.toLowerCase().trim();
    }

    private boolean areQueriesNotAllowed(final String nameQuery, final String emailQuery, final UserSearchParams userSearchParams)
    {
        return !userSearchParams.allowEmptyQuery() && StringUtils.isBlank(nameQuery) && (!userSearchParams.canMatchEmail() || StringUtils.isBlank(emailQuery));
    }

    private boolean userMatchesQueries(final User user, final UserSearchParams userSearchParams, final Predicate<User> userMatcher)
    {
        return (user.isActive() ? userSearchParams.includeActive() : userSearchParams.includeInactive())
                && userMatcher.apply(user);
    }

    private static final String VISIBILITY_PUBLIC = "show";
    private static final String VISIBILITY_USER = "user";
    private static final String VISIBILITY_MASKED = "mask";

    /**
     * @see UserPickerSearchService#canShowEmailAddresses(com.atlassian.jira.bc.JiraServiceContext)
     */
    @Override
    public boolean canShowEmailAddresses(final JiraServiceContext jiraServiceContext)
    {
        if (canPerformAjaxSearch(jiraServiceContext))
        {
            final String emailVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
            if (VISIBILITY_PUBLIC.equals(emailVisibility) || (VISIBILITY_MASKED.equals(emailVisibility)) || (VISIBILITY_USER.equals(emailVisibility) && (jiraServiceContext.getLoggedInApplicationUser() != null)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPerformAjaxSearch(final JiraServiceContext jiraServiceContext)
    {
        ApplicationUser loggedInUser = (jiraServiceContext != null) ? jiraServiceContext.getLoggedInApplicationUser() : null;
        return canPerformAjaxSearch(loggedInUser);
    }

    /**
     * @see UserPickerSearchService#canPerformAjaxSearch(com.atlassian.jira.bc.JiraServiceContext)
     */
    @Override
    public boolean canPerformAjaxSearch(final User user)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, user);
    }

    @Override
    public boolean canPerformAjaxSearch(final ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, user);
    }
}
