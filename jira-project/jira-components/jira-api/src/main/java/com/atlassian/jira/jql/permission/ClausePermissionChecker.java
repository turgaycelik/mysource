package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Set;

/**
 * Checks to see that the provided user is able to use the clause.
 *
 * @since v4.0
 */
public interface ClausePermissionChecker
{
    /**
     * Checks to see that the provided user is able to use the clause. This may be as simple as determining if the user
     * has permission to see the field that the clause represents.
     *
     * @param user to check permissions against.
     * @return true if the user can use this clause, false otherwise.
     */
    boolean hasPermissionToUseClause(User user);

    /**
     * Checks to see that the provided user is able to use the clause.
     * This method provides all the FieldLayouts visible to the given user as a shortcut otherwise individual checkers
     * can end up looking this up dozens or even hundreds of times with causes slow downs in our search (see JRADEV-15665).
     *
     * @param user to check permissions against.
     * @param fieldLayouts The field Layouts available to the given user (value of {@link com.atlassian.jira.issue.fields.FieldManager#getVisibleFieldLayouts(User)}.
     * @return true if the user can use this clause, false otherwise.
     */
    boolean hasPermissionToUseClause(User user, Set<FieldLayout> fieldLayouts);
}
