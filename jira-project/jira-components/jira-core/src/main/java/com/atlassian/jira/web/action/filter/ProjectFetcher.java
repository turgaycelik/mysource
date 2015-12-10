package com.atlassian.jira.web.action.filter;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Thin interface for retrieving Projects in lieu of sufficient domain object support
 * from ProjectManager or ProjectService.
 *
 * @since v3.13
 */
interface ProjectFetcher
{
    /**
     * Returns the projects which are in the given category.
     *
     * @param projectCategory the category or null for those projects in no category.
     * @return the projects.
     */
    Collection /*<Project>*/getProjectsInCategory(GenericValue projectCategory);

    /**
     * Returns the projects which are not in a category.
     *
     * @return the projects.
     */
    Collection /*<Project>*/getProjectsInNoCategory();

    /**
     * Returns true if there is at least one project somewhere.
     * @return true only if there is one or more projects in the system.
     */
    boolean projectsExist();
}
