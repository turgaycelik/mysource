package com.atlassian.jira.plugin.permission;

import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;

/**
 * Since v6.3.
 */
class DefaultProjectPermission implements ProjectPermission
{
    private final String key;
    private final String nameKey;
    private final String descriptionKey;
    private final ProjectPermissionCategory category;

    DefaultProjectPermission(String key, String nameKey, String descriptionKey, ProjectPermissionCategory category)
    {
        this.key = key;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.category = category;
    }

    public String getKey()
    {
        return key;
    }

    public String getNameI18nKey()
    {
        return nameKey;
    }

    public String getDescriptionI18nKey()
    {
        return descriptionKey;
    }

    public ProjectPermissionCategory getCategory()
    {
        return category;
    }
}