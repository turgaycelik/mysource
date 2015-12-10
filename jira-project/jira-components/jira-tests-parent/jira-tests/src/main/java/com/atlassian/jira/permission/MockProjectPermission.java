package com.atlassian.jira.permission;

/**
 * @since v6.3
 */
public class MockProjectPermission implements ProjectPermission
{
    private final String key;
    private final String nameKey;
    private final String descriptionKey;
    private final ProjectPermissionCategory category;

    public MockProjectPermission(String key, String nameKey, String descriptionKey, ProjectPermissionCategory category)
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
