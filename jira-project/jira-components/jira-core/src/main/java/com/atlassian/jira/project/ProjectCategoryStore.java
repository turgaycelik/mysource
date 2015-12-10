package com.atlassian.jira.project;

import java.util.List;

/**
 * Does DB operations for ProjectCategory.
 *
 * @since v4.4
 */
public interface ProjectCategoryStore
{
    ProjectCategory createProjectCategory(String name, String description);

    void removeProjectCategory(Long id);

    ProjectCategory getProjectCategory(Long id);

    /**
     * Returns all ProjectCategories, ordered by name.
     *
     * @return all ProjectCategories, ordered by name.
     */
    List<ProjectCategory> getAllProjectCategories();

    void updateProjectCategory(ProjectCategory projectCategory);
}
