package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
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
final public class GetActionsRequest
{
    private final Issue issue;
    private final User remoteUser;
    private final boolean asynchronous;
    private final boolean showAll;
    private final String focusId;

    @Internal
    public GetActionsRequest(@Nonnull Issue issue, @Nullable User remoteUser, boolean asynchronous, boolean showAll, @Nullable String focusId)
    {
        this.issue = Assertions.notNull(issue);
        this.remoteUser = remoteUser;
        this.asynchronous = asynchronous;
        this.showAll = showAll;
        this.focusId = focusId;
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

    /**
     * @return true if the actions are being loaded asynchronously, e.g. using an AJAX request
     */
    public boolean isAsynchronous()
    {
        return asynchronous;
    }

    public ApplicationUser loggedInUser()
    {
        return ApplicationUsers.from(remoteUser);
    }

    /**
     * @return true if all the actions should be returned
     *
     * Used by tabs that limit the number of actions to show (e.g. comments tab)
     */
    public boolean isShowAll()
    {
        return showAll;
    }

    /**
     * @return id of the action that should be focused
     * e.g. commentId for the comments tab
     *
     * Used by tabs that limit the number of actions to show, so that the focused action can always be displayed
     */
    public String getFocusId()
    {
        return focusId;
    }
}
