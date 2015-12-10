package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.api.User;

/**
 * An optional interface for {@link com.atlassian.jira.plugin.profile.ViewProfilePanel} modules that allows the panel
 * to only be show when a criteria is met.
 *
 * @since v4.1
 */
public interface OptionalUserProfilePanel
{
    /**
     * Whether or not to show the panel for a given user to a given user.
     *
     * @param profileUser The profile being requested
     * @param currentUser The current user
     * @return true if the panel should be show, otherwise false
     */
    boolean showPanel(User profileUser, User currentUser);
}
