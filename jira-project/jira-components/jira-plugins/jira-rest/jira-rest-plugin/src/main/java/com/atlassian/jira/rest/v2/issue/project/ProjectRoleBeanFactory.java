package com.atlassian.jira.rest.v2.issue.project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;

/**
 * @since v6.1
 */
public interface ProjectRoleBeanFactory
{
    ProjectRoleBean projectRole(@Nonnull Project project, @Nonnull ProjectRole projectRole);

    ProjectRoleBean projectRole(@Nonnull Project project, @Nonnull ProjectRole projectRole, @Nonnull ProjectRoleActors projectRoleActors, @Nullable User loggedInUser);
}
