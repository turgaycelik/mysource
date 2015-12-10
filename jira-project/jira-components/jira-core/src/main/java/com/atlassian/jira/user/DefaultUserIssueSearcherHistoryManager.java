package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.user.UserIssueSearcherHistoryManager}
 *
 * @since v6.0
 */
public class DefaultUserIssueSearcherHistoryManager implements UserIssueSearcherHistoryManager
{
    private final UserHistoryManager userHistoryManager;
    private ApplicationProperties applicationProperties;

    public DefaultUserIssueSearcherHistoryManager(final UserHistoryManager userHistoryManager, final ApplicationProperties applicationProperties)
    {
        this.userHistoryManager = userHistoryManager;
        this.applicationProperties = applicationProperties;
    }

    public void addIssueSearcherToHistory(final User user, final IssueSearcher searcher)
    {
        notNull("searcher", searcher);
        userHistoryManager.addItemToHistory(UserHistoryItem.ISSUESEARCHER, user, searcher.getSearchInformation().getId());
    }

    public List<UserHistoryItem> getUserIssueSearcherHistory(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.ISSUESEARCHER, user);
        int max = CachingUserHistoryStore.getMaxItems(UserHistoryItem.ISSUESEARCHER, applicationProperties);
        if(history.size() > max)
        {
            return history.subList(0, max);
        }
        return history;
    }
}
