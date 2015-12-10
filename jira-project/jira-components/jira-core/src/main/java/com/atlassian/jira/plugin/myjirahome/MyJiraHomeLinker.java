package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Retrieves a link representing the current My JIRA Home. In case non is found, it falls back to {@link #DEFAULT_HOME_NOT_ANON} or {@link #DEFAULT_HOME_OD_ANON}.
 *
 * @since 5.1
 */
public interface MyJiraHomeLinker
{
    String DEFAULT_HOME_NOT_ANON = "/secure/Dashboard.jspa";
    String DEFAULT_HOME_OD_ANON = "/login.jsp?os_destination=%2Fsecure%2FDashboard.jspa";
    
    /**
     * Returns the My JIRA Home as a link for the given user.
     *
     * @param user the user for which the home link is requested
     * @return the user's My JIRA Home, or one of {@link #DEFAULT_HOME_OD_ANON} if anonymous
     * or {@link #DEFAULT_HOME_NOT_ANON} if logged in if none is defined or there were errors while loading.
     */
    @Nonnull
    String getHomeLink(@Nullable User user);
}
