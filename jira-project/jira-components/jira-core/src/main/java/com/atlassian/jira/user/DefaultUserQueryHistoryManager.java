package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.user.UserQueryHistoryManager}
 *
 * @since v4.0
 */
public class DefaultUserQueryHistoryManager implements UserQueryHistoryManager
{
    private final UserHistoryManager userHistoryManager;
    private ApplicationProperties applicationProperties;

    public DefaultUserQueryHistoryManager(final UserHistoryManager userHistoryManager, final ApplicationProperties applicationProperties)
    {
        this.userHistoryManager = userHistoryManager;
        this.applicationProperties = applicationProperties;
    }

    public void addQueryToHistory(final User user, final String query)
    {
        notNull("query", query);

        userHistoryManager.addItemToHistory(UserHistoryItem.JQL_QUERY, user,  String.valueOf(query.hashCode()), query);
    }

    public List<UserHistoryItem> getUserQueryHistory(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.JQL_QUERY, user);
        int max = CachingUserHistoryStore.getMaxItems(UserHistoryItem.JQL_QUERY, applicationProperties);
        if(history.size() > max)
        {
            return history.subList(0, max);
        }
        return history;
    }
}
