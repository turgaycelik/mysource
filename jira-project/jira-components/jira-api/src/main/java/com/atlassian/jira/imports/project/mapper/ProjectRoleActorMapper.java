package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps data for the group/user project role membership.
 *
 * @since v3.13
 */
@PublicApi
public class ProjectRoleActorMapper
{
    private final Set<ExternalProjectRoleActor> projectRoleActors;

    public ProjectRoleActorMapper()
    {
        projectRoleActors = new HashSet<ExternalProjectRoleActor>();
    }

    public void flagValueActorAsInUse(final ExternalProjectRoleActor externalProjectRoleActor)
    {
        projectRoleActors.add(externalProjectRoleActor);
    }

    public Collection getAllProjectRoleActors()
    {
        return projectRoleActors;
    }
}
