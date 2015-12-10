package com.atlassian.jira.security.roles;

/**
 * Thrown if a user attemtps to add a Role Actor (Group or User) that does not exist.
 *
 * @since v4.0
 */
public class RoleActorDoesNotExistException extends Exception
{
    public RoleActorDoesNotExistException(final String message)
    {
        super(message);
    }
}
