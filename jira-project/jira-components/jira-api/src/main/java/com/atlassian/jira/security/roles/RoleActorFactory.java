package com.atlassian.jira.security.roles;

import com.atlassian.annotations.PublicSpi;

import java.util.Set;

/**
 * Create RoleActor instances.
 */
@PublicSpi
public interface RoleActorFactory
{
    /**
     * Create instances of the ProjectRoleActor
     *
     * @param id
     * @param projectRoleId
     * @param projectId
     * @param type
     * @param parameter
     *
     * @return
     */
    ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter) throws RoleActorDoesNotExistException;

    /**
     * if RoleActors can be aggregated and queried in a more optimised way, then optimize the set to reduce its size so
     * we reduce the number of iterations across the set.
     *
     * @param roleActors a Set of RoleActor instances
     *
     * @return the optimized Set perhaps containing aggregations that can be queried more efficiently.
     */
    Set<RoleActor> optimizeRoleActorSet(Set<RoleActor> roleActors);
}