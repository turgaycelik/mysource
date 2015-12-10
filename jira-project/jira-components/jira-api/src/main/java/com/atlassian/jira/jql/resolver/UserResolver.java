package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.List;

/**
 * We need a non-generified interface here to make this work with PICO+OSGi in plugins2
 * @since v4.0
 */
public interface UserResolver extends NameResolver<User>
{
    List<String> getIdsFromName(String name);

    boolean nameExists(String name);

    boolean idExists(Long id);

    /**
     * Get a user from an ID
     * @param id the id.
     * @return a User
     */
    User get(Long id);

    /**
     * Gets an application user from an ID
     * @param id the id.
     * @return an ApplicationUser
     */
    ApplicationUser getApplicationUser(Long id);

    /**
     * Get all users
     * @return All users
     */
    Collection<User> getAll();
}
