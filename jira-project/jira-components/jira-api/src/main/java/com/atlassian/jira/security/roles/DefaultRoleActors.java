package com.atlassian.jira.security.roles;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;
import java.util.Set;

/**
 * This interface defines the association between a ProjectRole and a collection of default Actors. Actors associated
 * with a ProjectRole in this way will be used to populate the ProjectRoleActors association when a new project is
 * created within JIRA.
 * <p/>
 * Note: implementations <strong>must be immutable</strong>.
 */
@PublicApi
public interface DefaultRoleActors
{
    Set<User> getUsers();
    Set<ApplicationUser> getApplicationUsers();

    Set<RoleActor> getRoleActors();

    Long getProjectRoleId();

    Set<RoleActor> getRoleActorsByType(String type);

    /**
     * Does the collection of RoleActor instances contain the specified user.
     *
     * @param user the application user to check if they are contained
     *
     * @return true if the user is matched by this RoleActor
     */
    boolean contains(ApplicationUser user);

    /**
     * Does the collection of RoleActor instances contain the specified user.
     *
     * @param user the application user to check if they are contained
     *
     * @return true if the user is matched by this RoleActor
     *
     * @deprecated Use contains(ApplicationUser user) instead. Since v6.0.
     */
    boolean contains(User user);

    /**
     * Add the RoleActor to the contained set of RoleActors and return a new DefaultRoleActors with the modified set
     *
     * @param roleActor the RoleActor to add
     *
     * @return a copy of this DefaultRoleActors with the added RoleActor in its set of RoleActors
     */
    DefaultRoleActors addRoleActor(RoleActor roleActor);

    /**
     * Add the collection of RoleActors to the contained set of RoleActors and return a new DefaultRoleActors with the
     * modified set
     *
     * @param roleActors the collection RoleActor to add
     *
     * @return a copy of this DefaultRoleActors with the added RoleActors in its set of RoleActors
     */
    DefaultRoleActors addRoleActors(Collection<? extends RoleActor> roleActors);

    /**
     * Remove the RoleActor from the contained set of RoleActors and return a new DefaultRoleActors with the modified
     * set
     *
     * @param roleActor the RoleActor to remove
     *
     * @return a copy of this DefaultRoleActors with the removed RoleActor in its set of RoleActors, may be this if
     *         unchanged
     */
    DefaultRoleActors removeRoleActor(RoleActor roleActor);

    /**
     * Remove the RoleActor from the contained set of RoleActors and return a new DefaultRoleActors with the modified
     * set
     *
     * @param roleActors the RoleActor to remove
     *
     * @return a copy of this DefaultRoleActors with the removed RoleActors in its set of RoleActors, may be this if
     *         unchanged.
     */
    DefaultRoleActors removeRoleActors(Collection<? extends RoleActor> roleActors);
}