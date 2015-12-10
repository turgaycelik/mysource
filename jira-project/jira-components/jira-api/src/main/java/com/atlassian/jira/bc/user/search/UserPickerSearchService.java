package com.atlassian.jira.bc.user.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

/**
 * Service that retrieves a collection of {@link User} objects based on a partial query string
 */
@PublicApi
public interface UserPickerSearchService
{
    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username and each word in Full Name & email.
     * Only returns active users.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserCachingComparator}.
     *
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @return List of {@link User} objects that match criteria.
     *
     * @see #findUsers(com.atlassian.jira.bc.JiraServiceContext, String, UserSearchParams)
     */
    List<User> findUsers(JiraServiceContext jiraServiceContext, String query);

    /**
     * Returns a user by exact username
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @return The {@link User} object with supplied username.
     */
    User getUserByName(JiraServiceContext jiraServiceContext, String query);

    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username and each word in Full Name & email.
     * This will search even if the query passed is null or empty.
     * Only returns active users.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserCachingComparator}.
     *
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @return List of {@link User} objects that match criteria.
     *
     * @see #findUsers(com.atlassian.jira.bc.JiraServiceContext, String, UserSearchParams)
     */
    List<User> findUsersAllowEmptyQuery(JiraServiceContext jiraServiceContext, String query);

    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username and each word in Full Name & email.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserCachingComparator}.
     *
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @param userSearchParams   Additional search parameters
     * @return List of {@link User} objects that match criteria.
     *
     * @since 5.1.5
     */
    List<User> findUsers(JiraServiceContext jiraServiceContext, String query, UserSearchParams userSearchParams);

    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username and each word in Full Name & email.
     * <p>
     * Matches email only when userSearchParams.canMatchEmail() is true.
     *
     * Results are sorted according to the userSearchParams.comparator.
     * If userSearchParams.comparator is null, no sorting will be performed.
     *
     * @param query the query to search username, display name and email address
     * @param userSearchParams the search criteria
     * @return the list of matched users
     *
     * @since 6.2
     */
    List<User> findUsers(String query, UserSearchParams userSearchParams);

    /**
     * Get Users based on query strings.
     * <p>
     * Matches nameQuery on the start of username and each word in Full Name & email.
     * <p>
     * Matches emailQuery on start of email. Email matching is performed only when userSearchParams.canMatchEmail() is true.
     *
     * Results are sorted according to the userSearchParams.comparator.
     * If userSearchParams.comparator is null, no sorting will be performed.
     *
     * @param nameQuery the query to search username and display name.
     * @param emailQuery the query to search email address, subject to userSearchParams.canMatchEmail.
     * @param userSearchParams the search criteria
     * @return the list of matched users
     *
     * @since 6.2
     */
    List<User> findUsers(String nameQuery, String emailQuery, UserSearchParams userSearchParams);

    /**
     * Determine whether a user matches the search criteria specified in the {@code userSearchParams} parameter.
     * <p>
     * allowEmptyQuery in {@code userSearchParams} is ignored.
     *
     * @param user the user to be matched
     * @param userSearchParams the search criteria
     * @return true if the user matches the search criteria
     *
     * @since v6.2
     */
    boolean userMatches(ApplicationUser user, UserSearchParams userSearchParams);

    /**
     * Returns true only if UserPicker Ajax search is enabled AND the user in the context has User Browse permission.
     *
     * @return True if enabled, otherwise false
     * @param jiraServiceContext Jira Service Context
     */
    boolean canPerformAjaxSearch(JiraServiceContext jiraServiceContext);

    /**
     * @deprecated since v6.2. Use {@link #canPerformAjaxSearch(com.atlassian.jira.user.ApplicationUser)} instead.
     */
    boolean canPerformAjaxSearch(User user);

    /**
     * Determines whether the given user could perform AJAX search.
     *
     * @since v6.2
     */
    boolean canPerformAjaxSearch(ApplicationUser user);

    /**
      * Whether or not the UserPicker Ajax should search or show email addresses
      *
      * @return True if email addresses can be shown, otherwise false
      * @param jiraServiceContext Jira Service Context
      */
    boolean canShowEmailAddresses(JiraServiceContext jiraServiceContext);
}
