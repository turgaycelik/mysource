package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

/**
 * A convenient mock for {@link ApplicationUser}.
 *
 * @since v6.0
 */
public class MockApplicationUser extends DelegatingApplicationUser
{
    /**
     * Uses the {@link IdentifierUtils#toLowerCase(String) lowercase} form of
     * the supplied {@code username} as the key.
     *
     * @param username as for {@link MockUser#MockUser(String)}
     */
    public MockApplicationUser(final String username)
    {
        super(IdentifierUtils.toLowerCase(username), new MockUser(username));
    }

    /**
     * Uses the {@link IdentifierUtils#toLowerCase(String) lowercase} form of
     * the supplied {@code username} as the key.
     *
     * @param username as for {@link MockUser#MockUser(String,String,String)}
     * @param displayName as for {@link MockUser#MockUser(String,String,String)}
     * @param email as for {@link MockUser#MockUser(String,String,String)}
     */
    public MockApplicationUser(final String username, final String displayName, final String email)
    {
        super(IdentifierUtils.toLowerCase(username), new MockUser(username, displayName, email));
    }

    /**
     * Uses the given key as-is.
     *
     * @param userKey desired user's Key, the value to be returned for {@link com.atlassian.jira.user.ApplicationUser#getKey()}
     * @param username as for {@link MockUser#MockUser(String)}
     */
    public MockApplicationUser(final String userKey, final String username)
    {
        super(userKey, new MockUser(username));
    }

    /**
     * Uses the given key as-is.
     *
     * @param userKey desired user's Key, the value to be returned for {@link com.atlassian.jira.user.ApplicationUser#getKey()}
     * @param username as for {@link MockUser#MockUser(String,String,String)}
     * @param displayName as for {@link MockUser#MockUser(String,String,String)}
     * @param email as for {@link MockUser#MockUser(String,String,String)}
     */
    public MockApplicationUser(final String userKey, final String username, final String displayName, final String email)
    {
        super(userKey, new MockUser(username, displayName, email));
    }
}
