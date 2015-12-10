package com.atlassian.jira.rest.v2.issue.project;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriInfo;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorComparator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.Transformed;

/**
 * @since v6.1
 */
public class ProjectRoleBeanFactoryImpl implements ProjectRoleBeanFactory
{
    private final UriInfo uriInfo;
    private final AvatarService avatarService;

    public ProjectRoleBeanFactoryImpl(final UriInfo uriInfo, final AvatarService avatarService)
    {
        this.uriInfo = uriInfo;
        this.avatarService = avatarService;
    }

    @Override
    public ProjectRoleBean projectRole(@Nonnull Project project, @Nonnull ProjectRole projectRole)
    {
        final ProjectRoleBean projectRoleBean = new ProjectRoleBean();
        projectRoleBean.name = projectRole.getName();
        projectRoleBean.id = projectRole.getId();
        projectRoleBean.description = projectRole.getDescription();
        projectRoleBean.self = uriInfo.getBaseUriBuilder().path(ProjectRoleResource.class).path(projectRoleBean.id.toString()).build(project.getId());
        return projectRoleBean;
    }

    @Override
    public ProjectRoleBean projectRole(@Nonnull final Project project, @Nonnull final ProjectRole projectRole, @Nonnull final ProjectRoleActors projectRoleActors, @Nullable final User loggedInUser)
    {

        // Sort the actors by name, as opposed to parameter
        final SortedSet<RoleActor> sortedActors = new TreeSet<RoleActor>(RoleActorComparator.COMPARATOR);
        sortedActors.addAll(projectRoleActors.getRoleActors());

        final Collection<RoleActorBean> actors = Transformed.collection(sortedActors, new Function<RoleActor, RoleActorBean>()
        {
            public RoleActorBean get(RoleActor actor)
            {
                final RoleActorBean bean = RoleActorBean.convert(actor);
                bean.setAvatarUrl(avatarService.getAvatarURL(loggedInUser, bean.getName(), Avatar.Size.SMALL));
                return bean;
            }
        });
        final ProjectRoleBean projectRoleBean = projectRole(project, projectRole);
        projectRoleBean.actors = actors;
        return projectRoleBean;
    }
}
