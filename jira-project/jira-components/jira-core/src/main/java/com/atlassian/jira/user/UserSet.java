package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps a unique set of Users based on the username only.
 * <p/>
 * This "collection" will work properly with shadowed users, mixed case usernames and the different hashcodes of
 * ApplicationUser and Directory Users.
 *
 * @since v6.0
 */
public class UserSet
{
    private final Map<String, User> userMap;

    public UserSet(Collection<User> users)
    {
        userMap = new HashMap<String, User>();

        for (User user : users)
        {
            add(user);
        }
    }

    public boolean contains(User user)
    {
        return userMap.containsKey(lowerUsername(user));
    }

    public void add(User user)
    {
        userMap.put(lowerUsername(user), user);
    }

    public Collection<User> values()
    {
        return userMap.values();
    }

    public Set<User> toSet()
    {
        return new HashSet<User>(values());
    }

    private String lowerUsername(User user)
    {
        return IdentifierUtils.toLowerCase(user.getName());
    }
}
