package com.atlassian.jira.security.roles;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @see ProjectRoleActors
 */
public class ProjectRoleActorsImpl extends DefaultRoleActorsImpl implements ProjectRoleActors
{
    private final Long projectId;

    public ProjectRoleActorsImpl(Long projectId, Long projectRoleId, Set<? extends RoleActor> roleActors)
    {
        super(projectRoleId, roleActors);
        this.projectId = projectId;
    }

    public ProjectRoleActorsImpl(Long projectId, Long projectRoleId, RoleActor roleActor)
    {
        super(projectRoleId, roleActor);
        this.projectId = projectId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public DefaultRoleActors addRoleActor(RoleActor roleActor)
    {
        Set<RoleActor> set = newHashSet(getRoleActors());
        set.add(roleActor);
        return new ProjectRoleActorsImpl(projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors addRoleActors(Collection<? extends RoleActor> roleActors)
    {
        final Set<RoleActor> set = newHashSet(getRoleActors());
        set.addAll(roleActors);
        return new ProjectRoleActorsImpl(projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors removeRoleActor(RoleActor roleActor)
    {
        final Set<RoleActor> roleActors = getRoleActors();
        if (!roleActors.contains(roleActor))
        {
            return this;
        }
        final Set<RoleActor> set = newHashSet(roleActors);
        set.remove(roleActor);
        return new ProjectRoleActorsImpl(projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors removeRoleActors(Collection<? extends RoleActor> roleActors)
    {
        final Set<RoleActor> set = newHashSet(getRoleActors());
        set.removeAll(roleActors);
        return new ProjectRoleActorsImpl(projectId, getProjectRoleId(), set);
    }

    @Override
    public String toString()
    {
        return "ProjectRoleActorsImpl[projectId=" + projectId + ",super=" + super.toString() + ']';
    }
}