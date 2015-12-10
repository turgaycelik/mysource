package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * A simple service to map userkeys to usernames.
 *
 * @since v6.0
 */
public interface UserKeyService
{
    /**
     * Returns the (lower-cased) username that is associated with the given key.  Normally this mapping
     * is retained even for deleted users.
     * <p>
     * Note that this returns the lower-case of the username (because username must act case-insensitive in our key->username map).
     *
     * @param key the key to resolve to a username (may be {@code null})
     * @return the username that is currently associated with the key, or {@code null} if {@code key}
     *      is {@code null} or unmapped.  Note that a non-{@code null} result does not guarantee that
     *      the user still exists.
     */
    String getUsernameForKey(String key);

    /**
     * Returns the key that is associated with the given username.  Normally this mapping
     * is retained even for deleted users.
     *
     * @param username the username to resolve to a key (may be {@code null})
     * @return the key that is currently associated with the username, or {@code null} if {@code username}
     *      is {@code null} or unmapped.  Note that a non-{@code null} result does not guarantee that
     *      the user still exists.
     */
    String getKeyForUsername(String username);

    /**
     * This convenience method is equivalent to {@code getKeyForUsername(user.getName())}, except that
     * it is {@code null}-safe.
     * @param user the user to resolve to a key (may be {@code null})
     * @return as for {@link #getKeyForUsername(String)}
     */
    String getKeyForUser(User user);
}
