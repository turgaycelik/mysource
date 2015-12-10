package com.atlassian.jira.config;

import com.atlassian.jira.issue.status.category.StatusCategory;

import java.util.List;

/**
 * Manager for {@link StatusCategory}ies.
 *
 * @since v6.1
 */
public interface StatusCategoryManager
{

    /**
     * Get all status categories
     *
     * @return a list of all StatusCategories
     */
    List<StatusCategory> getStatusCategories();

    /**
     * Get status categories visible to any user
     *
     * @return a list of StatusCategories visible to any user
     */
    List<StatusCategory> getUserVisibleStatusCategories();

    /**
     * Get {@link StatusCategory} which is default for statuses
     *
     * @return StatusCategory
     */
    StatusCategory getDefaultStatusCategory();

    /**
     * Find category by given ID
     *
     * @param id the id of the category
     * @return category or null when is not found
     */
    StatusCategory getStatusCategory(Long id);

    /**
     * Find category by given Key
     * @param key the key of the category
     * @return category or null when is not found
     */
    StatusCategory getStatusCategoryByKey(String key);

    /**
     * Find category by given Name
     * @param name the non-i18n name of the category
     * @return category or null when is not found
     * @since v6.2
     */
    StatusCategory getStatusCategoryByName(String name);

    /**
     * Returns a boolean whether status lozenge is enabled or not
     *
     * @return a boolean
     */
    boolean isStatusAsLozengeEnabled();

}
