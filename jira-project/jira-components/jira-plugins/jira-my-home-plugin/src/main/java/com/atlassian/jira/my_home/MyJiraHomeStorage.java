package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;

/**
 * Loads and stores the My JIRA Home for the given user.
 */
public interface MyJiraHomeStorage
{
    /**
     * Returns the home stored for the given user.
     *
     * @param user the user for which the home is requested
     * @return the home if found, else an empty string
     */
    @Nonnull
    String load(@Nonnull User user);

    /**
     * Stores the given home for the given user.
     *
     * @param user the user for which the home is stored
     * @param home the actual home to be stored
     *
     * @throws com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException if the update fails
     */
    void store(@Nonnull User user, @Nonnull String home);
}
