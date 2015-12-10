package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;

import java.util.List;

/**
 * A wrapper of the {@link UserHistoryManager} that allows you to store and retrieve history items {@link com.atlassian.jira.user.UserHistoryItem} of the type ISSUESEARCHER.
 *
 * @since v5.2
 */
public interface UserIssueSearcherHistoryManager
{
    /**
     * Add a JQL query string to the user history list.
     *
     * @param user           The user to add the history item to
     * @param searcher       The {@link IssueSearcher} used.
     */
    void addIssueSearcherToHistory(User user, IssueSearcher searcher);

    /**
     * Retrieve the user's Issue Seacher history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     *
     * @param user  The user to get the history IssueSearcher items for.
     * @return a list of history IssueSearcher items sort by desc lastViewed date.
     */
    List<UserHistoryItem> getUserIssueSearcherHistory(User user);

}
