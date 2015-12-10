package com.atlassian.jira.rest.v2.issue.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.rest.v2.issue.UserPickerResultsBean;

import java.util.List;

public interface UserPickerResourceHelper
{
    /**
     * Returns a list of users matching query with highlighting. This resource cannot be accessed anonymously.
     *
     *
     * @param query A string used to search username, Name or e-mail address. Empty query is not allowed.
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param showAvatar get avatar url
     * @param excludeUsers exclude these users from search results
     * @return An object containing list of matched user objects, with html representing highlighting.
     */
    UserPickerResultsBean findUsersAsBean(String query, Integer maxResults, Boolean showAvatar, List<String> excludeUsers);

    /**
     * Returns a list of users matching query with highlighting. This resource cannot be assessed anonymously.
     *
     * @param query A string used to search username, Name or e-mail address. Empty query is not allowed.
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param showAvatar get avatar url
     * @param excludeUsers exclude these users from search results
     * @param userSearchParams additional search parameters. allowEmptyQuery is ignored, always treated as false.
     * @return An object containing list of matched user objects, with html representing highlighting.
     *
     * @since v6.1
     */
    UserPickerResultsBean findUsersAsBean(String query, Integer maxResults, Boolean showAvatar, List<String> excludeUsers, UserSearchParams userSearchParams);

    /**
     *
     * @param startAt start the search at this index
     * @param maxResults max results
     * @param users search results to limit
     * @param excludeUsers exclude these users from search results
     * @return adjusted search results
     */
    List<User> limitUserSearch(Integer startAt, Integer maxResults, Iterable<User> users, Iterable<String> excludeUsers);

    /**
     * Finds active users, throws exception if search string is null
     *
     * @param searchString user query
     * @return List of matching users
     */
    List<User> findActiveUsers(String searchString);

    /**
     * Finds users, throws exception if search string is null
     *
     * @param searchString user query, no users returned if empty
     * @param includeActive whether to include active users (null implies true)
     * @param includeInactive whether to include inactive users (null implies false)
     *
     * @return List of matching users
     */
    List<User> findUsers(String searchString, Boolean includeActive, Boolean includeInactive);

    /**
     * Finds users, throws exception if search string is null
     *
     * @param searchString user query
     * @param userSearchParams search parameters, including "includeActive", "includeInactive", etc. allowEmptyQuery is ignored, always treated as false.
     *
     * @return List of matching users
     */
    List<User> findUsers(String searchString, UserSearchParams userSearchParams);

    /**
     * Finds users, throws exception if search string is null
     *
     * @param searchString user query
     * @param allowEmptySearchString controls whether users are returned if searchString is empty
     * @param includeActive whether to include active users (null implies true)
     * @param includeInactive whether to include inactive users (null implies false)
     *
     * @return List of matching users
     */
    List<User> findUsers(String searchString, Boolean includeActive, Boolean includeInactive, boolean allowEmptySearchString);

    /**
     * Returns a user if supplied userName is exact match to their username.
     *
     * @param userName The username query
     * @return User The exact user or null if not found.
     */
    User getUserByName(String userName);
}
