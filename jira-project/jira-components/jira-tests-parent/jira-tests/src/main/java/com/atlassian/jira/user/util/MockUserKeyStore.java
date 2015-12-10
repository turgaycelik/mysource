package com.atlassian.jira.user.util;


import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

/**
 * @since v6.0
 */
public class MockUserKeyStore implements UserKeyStore
{
    private boolean useDefaultMapping = true;
    private Map<String, String> keyToUsernameMap = new HashMap<String, String>();
    private Map<String, String> usernameToKeyMap = new HashMap<String, String>();

    @Override
    public String getUsernameForKey(String key)
    {
        if (useDefaultMapping)
            return key;
        else
            return keyToUsernameMap.get(key);
    }

    @Override
    public String getKeyForUsername(String username)
    {
        if (username == null)
        {
            return null;
        }
        String lowerUsername = IdentifierUtils.toLowerCase(username);
        if (username == null)
        {
            return null;
        }
        if (useDefaultMapping)
            return lowerUsername;
        else
            return usernameToKeyMap.get(lowerUsername);
    }

    @Override
    public void renameUser(String oldUsername, String newUsername)
    {
        // Lower-case the usernames
        oldUsername = IdentifierUtils.toLowerCase(oldUsername);
        newUsername = IdentifierUtils.toLowerCase(newUsername);

        final String userkey = usernameToKeyMap.remove(oldUsername);
        usernameToKeyMap.put(newUsername, userkey);
        keyToUsernameMap.put(userkey, newUsername);
    }

    @Override
    public String ensureUniqueKeyForNewUser(String username)
    {
        String lowerUsername = IdentifierUtils.toLowerCase(username);
        if (useDefaultMapping)
        {
            return lowerUsername;
        }
        else
        {
            if (usernameToKeyMap.containsKey(lowerUsername))
            {
                // nothing to do
                return usernameToKeyMap.get(lowerUsername);
            }
            final String userkey;
            // Can we use the name as the key?
            if (keyToUsernameMap.containsKey(lowerUsername))
            {
                // Tricky
                // In production we use the ID in the DB, lets get something close
                userkey = findUniqueKey();
            }
            else
            {
                // Simple
                userkey = lowerUsername;
            }
            usernameToKeyMap.put(lowerUsername, userkey);
            keyToUsernameMap.put(userkey, lowerUsername);
            return userkey;
        }
    }

    @Override
    public String removeByKey(final String key)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setMapping(String key, String username)
    {
        keyToUsernameMap.put(key, username);
        usernameToKeyMap.put(IdentifierUtils.toLowerCase(username), key);
    }

    private String findUniqueKey()
    {
        int id = 10001;
        while (id < 11000)
        {
            String potentialKey = "XYZ" + id;
            if (!keyToUsernameMap.containsKey(potentialKey))
                return potentialKey;
            id++;
        }
        throw new IllegalStateException("WTF!");
    }

    @Override
    public Long getIdForUserKey(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void useDefaultMapping(boolean useDefaultMapping)
    {
        this.useDefaultMapping = useDefaultMapping;
    }
}
