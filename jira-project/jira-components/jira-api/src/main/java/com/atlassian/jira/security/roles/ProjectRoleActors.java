package com.atlassian.jira.security.roles;

import com.atlassian.annotations.PublicApi;

/**
 * This interface defines the association between a ProjectRole and a collection of Actors for a project.
 */
@PublicApi
public interface ProjectRoleActors extends DefaultRoleActors
{
    Long getProjectId();
}