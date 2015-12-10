package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;

import java.util.HashMap;
import java.util.Map;

public class MockUserKeyService implements UserKeyService
{
    private Map<String, String> userkeyToNameMap = new HashMap<String, String>();
    private Map<String, String> usernameToKeyMap = new HashMap<String, String>();

    @Override
    public String getUsernameForKey(String key)
    {
        final String name = userkeyToNameMap.get(key);
        return (name != null) ? name : key;
    }

    @Override
    public String getKeyForUsername(String username)
    {
        if (username == null)
        {
            return null;
        }
        username = IdentifierUtils.toLowerCase(username);
        final String key = usernameToKeyMap.get(username);
        return (key != null) ? key : username;
    }

    @Override
    public String getKeyForUser(User user)
    {
        if (user == null)
            return null;
        return getKeyForUsername(user.getName());
    }

    public void setMapping(String key, String username)
    {
        userkeyToNameMap.put(key, username);
        usernameToKeyMap.put(IdentifierUtils.toLowerCase(username), key);
    }
}
