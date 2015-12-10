package com.atlassian.jira.bc.project.component;

import java.util.Collection;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.ErrorCollection;

@PublicApi
public interface ProjectComponentService
{
    /**
     * Create a project component and use default assignee type (project default).
     *
     * @return A newly created project component {@link ProjectComponent}
     * @deprecated since 6.3, use {@link #create(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.util.ErrorCollection, String, String, String, Long, Long)} instead
     */
    @Deprecated
    ProjectComponent create(User user, ErrorCollection errorCollection, String name, String description, String lead, Long projectId);

    /**
     * Create a project component with assignee type set as provided.
     *
     * @return A newly created project component {@link ProjectComponent}
     */
    ProjectComponent create(User user, ErrorCollection errorCollection, String name, String description, String lead, Long projectId, Long assigneeType);

    ProjectComponent find(User user, ErrorCollection errorCollection, Long id);

    Collection<ProjectComponent> findAllForProject(ErrorCollection errorCollection, Long projectId);

    ProjectComponent update(User user, ErrorCollection errorCollection, MutableProjectComponent component);

    void deleteComponentForIssues(JiraServiceContext context, Long componentId);

    void deleteAndSwapComponentForIssues(JiraServiceContext context, Long componentId, Long swapComponentId);
}
