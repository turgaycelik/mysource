package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;

/**
 * A class that respresents a small piece of a screen on a User Profile Tab that is fragment based.
 */
public interface UserProfileFragment
{
    /**
     * Whether or not we display this fragment.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return true if we should display this fragment, otherwise false
     * @since v4.3
     */
    public boolean showFragment(User profileUser, User currentUser);

    /**
     * Creates the HTML for this fragment.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return The HTML of this fragment
     * @since v4.3
     */
    public String getFragmentHtml(User profileUser, User currentUser);

    /**
     * Returns a unique id for this fragment.  The id should be HTML compliant for ids.
     *
     * @return a unique id for this fragment
     */
    public String getId();
}
