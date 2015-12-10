package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * An interface that lists the methods assumed to exist when the action runs under the context of a UserProfile.
 * This should introduce some type safety to these actions.
 *
 * @since v3.13
 */
public interface UserProfileAction
{
    /**
     * Get a string that can be used to describe the passed e-mail. This allows JIRA to hide the passed e-mail if
     * asked to do so.
     *
     * @param email the e-mail to convert.
     * @return the encoded e-mail address.
     */
    String getDisplayEmail(String email);

    /**
     * Returns whether or not the passed caller is allowed to see the passed group.
     *
     * @param group the name of the group to check.
     * @param user the user to check.
     *
     * @return true if the user can see the group or false otherwise.
     */
    boolean isHasViewGroupPermission(String group, User user);

    /**
     * Return the current user.
     *
     * @return the current user.
     */
    User getUser();
}
