package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * Helper class to extract projects from the search result context.
 *
 * @since v3.13
 */
public interface ProjectAwareQuickSearchHandler
{
    /**
     * Retrieve the related project generic values from the search result
     *
     * @param searchResult search result to extract related project information from
     * @return all projects that may be related to this search
     */
    List/*<GenericValue>*/getProjects(QuickSearchResult searchResult);

    /**
     * Retrieves the single project specified in search.
     * @param searchResult search result to extract the project from
     * @return the ID of the single project in search. Null if there is no project of more than 1 project
     */
    String getSingleProjectIdFromSearch(QuickSearchResult searchResult);

    /**
     * Adds a project to the given search.
     * @param projectId project id to add
     * @param searchResult search result to add to
     */
    void addProject(String projectId, QuickSearchResult searchResult);
}
