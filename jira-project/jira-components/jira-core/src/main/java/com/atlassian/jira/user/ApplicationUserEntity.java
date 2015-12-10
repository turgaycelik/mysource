package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

/**
 * @since v6.0
 */
public final class ApplicationUserEntity
{
    private final Long id;
    private final String key;
    private final String username;

    public ApplicationUserEntity(Long id, String key, String username)
    {
        this.id = id;
        this.key = key;
        this.username = IdentifierUtils.toLowerCase(username);
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    /**
     * Returns the lower-case of the username (because username must act case-insensitive).
     * @return the lower-case of the username (because username must act case-insensitive).
     */
    public String getUsername()
    {
        return username;
    }
}
