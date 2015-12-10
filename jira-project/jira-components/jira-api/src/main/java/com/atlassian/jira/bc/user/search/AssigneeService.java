package com.atlassian.jira.bc.user.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import javax.annotation.Nullable;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service that retrieves issue-assignable {@link User} objects.
 * <p>
 * Assignees may be filtered on a search string or on recent issue or user history.
 *
 * @since v5.0
 */
@PublicApi
public interface AssigneeService
{
    /**
     * Get all {@link User}s that may have an {@link Issue} assigned to them, for a given workflow state.
     * <p>
     * The {@link ActionDescriptor} may be used to check for workflow states that only allow a subset of
     * normally-assignable users.
     *
     * @param issue the Issue to find assignable users for
     * @param actionDescriptor workflow action descriptor to filter users on
     * @return a list of Users sorted by name
     */
    List<User> getAssignableUsers(Issue issue, @Nullable ActionDescriptor actionDescriptor);

    /**
     * Get all {@link User}s that may have all of the given {@link Issue}s assigned to them, for a
     * given workflow state.
     * <p>
     * The {@link ActionDescriptor} may be used to check for workflow states that only allow a subset of
     * normally-assignable users.
     * </p><p>
     * Note: This method is exactly equivalent to {@link #getAssignableUsers(Issue, ActionDescriptor)},
     * but returns only those users that are assignable for <em>all</em> of the issues.  This is
     * significantly more efficient than calling {@link #getAssignableUsers(Issue, ActionDescriptor)}
     * multiple times and filtering the lists yourself.
     * </p>
     *
     * @param issues the Issues to find assignable users for
     * @param actionDescriptor workflow action descriptor to filter users on
     * @return a list of Users sorted by name
     */
    List<User> getAssignableUsers(Collection<Issue> issues, @Nullable ActionDescriptor actionDescriptor);

    /**
     * Get assignable Users based on a query string and issue.
     * <p>
     * Matches on the start of username, Each word in Full Name & email.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserCachingComparator}.
     *
     *
     * @param query             String to search for.
     * @param issue             Issue to check Assignee permissions against
     * @param actionDescriptor  an {@link com.opensymphony.workflow.loader.ActionDescriptor} describing the context in which the Assignee is being searched
     * @return List of {@link User} objects that match criteria.
     */
    Collection<User> findAssignableUsers(String query, Issue issue, ActionDescriptor actionDescriptor);

    /**
     * Get assignable Users based on a query string and project.
     * <p>
     * Matches on the start of username, Each word in Full Name & email.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserCachingComparator}.
     *
     *
     * @param query             String to search for.
     * @param project           Project check Assignee permissions against
     * @return List of {@link User} objects that match criteria.
     */
    Collection<User> findAssignableUsers(String query, Project project);

    /**
     * Returns a list of suggested Assignee {@link User}s for a given Issue and logged-in User.
     * <p>
     * This version accepts a pre-populated list of assignable Users to filter.
     *
     * @param issue Issue to get suggested Assignees for
     * @param loggedInUser the user getting the suggestions, whose Assignee history may be queried
     * @param actionDescriptor workflow action descriptor to filter users on
     * @return List of {@link User} objects deemed relevant to the given Issue and User, sorted by UserBestNameComparator
     */
    List<User> getSuggestedAssignees(Issue issue, @Nullable User loggedInUser, @Nullable ActionDescriptor actionDescriptor);

    /**
     * Returns a list of suggested Assignee {@link User}s for a given Issue and logged-in User.
     * <p>
     * This version accepts a pre-populated list of assignable Users to filter.
     *
     * @param issue Issue to get suggested Assignees for
     * @param loggedInUser the user getting the suggestions, whose Assignee history may be queried
     * @param assignableUsers a list of {@link User}s to filter
     * @return List of {@link User} objects deemed relevant to the given Issue and User.
     */
    List<User> getSuggestedAssignees(Issue issue, User loggedInUser, List<User> assignableUsers);


    /**
     * Converts a collection of Users to a Map where the key is the User full name and the value is true or false.
     * <p>
     * The value will be true if no other user with that exact full name exists.
     * The value will be false if at least one other user with that exact full name exists.
     *
     * @param users a collection of Users that may contain multiple users with the same full name
     * @return a map of user full name Strings to a uniqueness boolean flag
     */
    Map<String, Boolean> makeUniqueFullNamesMap(Collection<User> users);

    /**
     * Converts a set of suggested assignee name Strings to a list of suggested {@link User} objects.
     * <p>
     * Suggested user names may not be returned as suggested users if they are not in the assignable user list.
     *
     * @param suggestedAssigneeNames the names of the users to return
     * @param assignableUsers a list of Users to filter by the suggested assignee names
     * @return a filtered List of assignable Users that are suggested as Assignees
     */
    List<User> getSuggestedAssignees(final Set<String> suggestedAssigneeNames, List<User> assignableUsers);

    /**
     * Returns the names of Users that the given Issue has recently been assigned to.
     * <p>
     * The current assignee should be included in the returned list.
     *
     * @param issue Issue to get recent assignees for
     * @return a Set of assignee usernames
     * @see #getRecentAssigneeKeysForIssue(com.atlassian.jira.issue.Issue)
     */
    Set<String> getRecentAssigneeNamesForIssue(Issue issue);

    /**
     * Returns the names of {@link User}s that have recently been assigned to issues by the specified User.
     *
     * @param user User to check for assignees in history manager
     * @return a Set of assignee usernames
     */
    Set<String> getRecentAssigneeNamesForUser(User user);

    /**
     * Returns the keys of Users that the given Issue has recently been assigned to.
     * <p>
     * The current assignee should be included in the returned list.
     *
     * @param issue Issue to get recent assignees for
     * @return a Set of assignee keys
     * @see #getRecentAssigneeNamesForIssue(com.atlassian.jira.issue.Issue)
     */
    Set<String> getRecentAssigneeKeysForIssue(Issue issue);

    /**
     * Returns the keys of {@link User}s that have recently been assigned to issues by the specified User.
     *
     * @param remoteUser User to check for assignees in history manager
     * @return a Set of assignee usernames
     */
    Set<String> getRecentAssigneeKeysForUser(User remoteUser);
}
