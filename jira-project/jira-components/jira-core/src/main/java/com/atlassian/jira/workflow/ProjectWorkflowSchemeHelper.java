package com.atlassian.jira.workflow;

import com.atlassian.jira.project.Project;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Set;

/**
 * Provides the ability to deromilize the workflow -> workflowscheme ->project relationship in a number of different ways.
 *
 * This is really an internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectWorkflowSchemeHelper
{
    /**
     * Return a list of projects that use the workflow scheme with the passed query. Only projects that the passed user
     * can change the configurtion for will be returned.
     *
     * @param schemeId the scheme id query. Null can be passed to search for the magical default workflow scheme.
     *
     * @return a list of projects that use the workflow. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForScheme(Long schemeId);

    /**
     * Returns a list of the projects are currently using the passed workflow. A workflow is only considered "active"
     * if it is currently actually being used by a project. This means it is active if:
     *
     * <ol>
     *     <li>The workflow is mapped to an issue type in the project's scheme and the project has that issue type.
     *     <li>The workflow is a default for the project's scheme and there are some unmapped issue types.
     * </ol>
     *
     * Only projects that the passed user can change the configurtion for will be returned.
     *
     * @param workflowName the name of the workflow the check.
     *
     * @return a list of active projects. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForWorkflow(String workflowName);

    /**
     * Return a map of active workflow names to the projects that use those workflows. A workflow is only considered
     * "active" if it is currently actually being used by a project. This means it is active if:
     *
     * <ol>
     *     <li>The workflow is mapped to an issue type in the project's scheme and the project has that issue type.
     *     <li>The workflow is a default for the project's scheme and there are some unmapped issue types.
     * </ol>
     *
     * Only projects that the passed user can change the configurtion for will be returned.
     *
     * @param workflows the workflows to query.
     *
     * @return a list of active projects. The projects are sorted by name. The map is mutable and can be changed by
     * the caller.
     */
    Multimap<String, Project> getProjectsForWorkflow(Set<String> workflows);
}
