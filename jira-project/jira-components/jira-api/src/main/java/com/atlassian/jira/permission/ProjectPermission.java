package com.atlassian.jira.permission;

/**
 * Represents a project permission.
 *
 * @since v6.3
 */
public interface ProjectPermission
{
    /**
     * @return unique key of this project permission.
     * @since v6.3
     */
    String getKey();

    /**
     * @return i18n key of this permission's name. Cannot be null.
     * @since v6.3
     */
    String getNameI18nKey();

    /**
     * @return i18n key of this permission's description. Can be null.
     * @since v6.3
     */
    String getDescriptionI18nKey();

    /**
     * @return category of this permission. Cannot be null.
     * @since v6.3
     */
    ProjectPermissionCategory getCategory();
}
