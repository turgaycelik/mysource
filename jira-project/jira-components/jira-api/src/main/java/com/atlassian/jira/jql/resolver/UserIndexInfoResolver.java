package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.UserKeyService;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for users.
 * <p>
 * The indexed value for a user is the user's key.
 * <strong>WARNING</strong>: Prior to JIRA 6.0, this was always the lowercase
 * version of the username, but this will not be true for renamed users.
 * Plugin developers should avoid relying on the exact contents of the index value
 * directly.
 * </p>
 *
 * @since v4.0
 */
public class UserIndexInfoResolver implements IndexInfoResolver<User>
{
    private final NameResolver<User> userResolver;

    public UserIndexInfoResolver(final NameResolver<User> userResolver)
    {
        this.userResolver = userResolver;
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        return userResolver.getIdsFromName(notNull("rawValue", rawValue));
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        return getIndexedValues(notNull("rawValue", rawValue).toString());
    }

    public String getIndexedValue(final User user)
    {
        return getUserKeyService().getKeyForUsername(notNull("user", user).getName());
    }

    UserKeyService getUserKeyService()
    {
        return ComponentAccessor.getComponentOfType(UserKeyService.class);
    }
}