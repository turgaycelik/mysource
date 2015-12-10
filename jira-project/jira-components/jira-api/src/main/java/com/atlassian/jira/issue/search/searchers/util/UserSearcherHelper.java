package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public interface UserSearcherHelper
{
    public void addUserSuggestionParams(User user, List<String> selectedUsers, Map<String, Object> params);
    public void addGroupSuggestionParams(User user, Map<String, Object> params);
    public void addUserGroupSuggestionParams(User user, List<String> selectedUsers, Map<String, Object> params);

    /**
     * add user and group suggestions based on search parameters.
     * the parameters will be added into the {@code params} parameter in-place.
     *
     * @param user the user requesting for the suggestions
     * @param selectedUsers a list of recently selected users, which could be included into the suggested users with higher priority
     * @param searchParams additional search parameters for groups and roles based restrictions.
     * @param params the map to hold the parameters
     */
    public void addUserGroupSuggestionParams(User user, List<String> selectedUsers, UserSearchParams searchParams, Map<String, Object> params);

    /**
     * @deprecated since v6.2. Use {@link #hasUserPickingPermission(com.atlassian.jira.user.ApplicationUser)} instead.
     */
    public boolean hasUserPickingPermission(User user);

    /**
     * Determine whether a user has permission to pick users.
     * @since v6.2
     */
    public boolean hasUserPickingPermission(ApplicationUser user);
}
