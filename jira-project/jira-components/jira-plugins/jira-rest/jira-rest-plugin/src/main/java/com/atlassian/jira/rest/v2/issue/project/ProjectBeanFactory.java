package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.project.Project;

import javax.annotation.Nonnull;

/**
 * Class for creating project from projects.
 *
 * @since v4.4
 */
public interface ProjectBeanFactory
{
    ProjectBean fullProject(@Nonnull Project project, @Nonnull String expand);

    ProjectBean shortProject(@Nonnull Project project);
}
