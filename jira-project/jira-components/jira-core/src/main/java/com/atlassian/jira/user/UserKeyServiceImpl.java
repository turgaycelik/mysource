package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserKeyStore;

public class UserKeyServiceImpl implements UserKeyService
{
    private final UserKeyStore userKeyStore;

    public UserKeyServiceImpl(UserKeyStore userKeyStore)
    {
        this.userKeyStore = userKeyStore;
    }

    @Override
    public String getUsernameForKey(String key)
    {
        return userKeyStore.getUsernameForKey(key);
    }

    @Override
    public String getKeyForUsername(String username)
    {
        return userKeyStore.getKeyForUsername(username);
    }

    @Override
    public String getKeyForUser(User user)
    {
        if (user == null)
        {
            return null;
        }
        return getKeyForUsername(user.getName());
    }
}
