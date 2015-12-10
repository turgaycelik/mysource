package com.atlassian.jira.issue.comments;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comments.util.CommentIterator;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;

/**
 * A standard way of getting a list of recent comments from JIRA
 */
@PublicApi
public interface RecentCommentManager
{
    /**
     * Return an iterator over all the comments from any issues in the search request that the user can see
     *
     * @param searchRequest
     *            The search request to limit the comments to
     * @param user
     *            The {@link ApplicationUser} to match the Permissions against
     * @return
     * @throws com.atlassian.jira.issue.search.SearchException Exceptions occured while trying to peform a search on the {@link com.atlassian.jira.issue.search.SearchRequest}
     */
    public CommentIterator getRecentComments(SearchRequest searchRequest, ApplicationUser user) throws SearchException;

    /**
     * @deprecated Use {@link #getRecentComments(com.atlassian.jira.issue.search.SearchRequest, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Return an iterator over all the comments from any issues in the search request that the user can see
     *
     * @param searchRequest
     *            The search request to limit the comments to
     * @param user
     *            The {@link User} to match the Permissions against
     * @return
     * @throws com.atlassian.jira.issue.search.SearchException Exceptions occured while trying to peform a search on the {@link com.atlassian.jira.issue.search.SearchRequest}
     */
    public CommentIterator getRecentComments(SearchRequest searchRequest, User user) throws SearchException;
}
