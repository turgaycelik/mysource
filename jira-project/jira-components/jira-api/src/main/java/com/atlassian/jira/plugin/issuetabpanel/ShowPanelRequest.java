package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.dbc.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Request object used in the {@link IssueTabPanel2} SPI.
 *
 * @see IssueTabPanel2
 * @since v5.0
 */
@PublicApi
@Immutable
public final class ShowPanelRequest
{
    private final Issue issue;
    private final User remoteUser;

    @Internal
    public ShowPanelRequest(@Nonnull Issue issue, @Nullable User remoteUser)
    {
        this.issue = Assertions.notNull(issue);
        this.remoteUser = remoteUser;
    }

    /**
     * @return the Issue on which the panel will be displayed
     */
    @Nonnull
    public Issue issue()
    {
        return issue;
    }

    /**
     * @return the User that is viewing the page, or null for an anonymous user
     */
    @Nullable
    public User remoteUser()
    {
        return remoteUser;
    }

    /**
     * @return true iff the user that is viewing the page is anonymous (i.e. not logged in)
     */
    public boolean isAnonymous()
    {
        return remoteUser() == null;
    }
}
