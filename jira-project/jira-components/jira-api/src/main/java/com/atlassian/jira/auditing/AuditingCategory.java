package com.atlassian.jira.auditing;

import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Possible values for auditing category.
 *
 * @since v6.2
 */
@ExperimentalApi
public enum AuditingCategory
{
    AUDITING("auditing", "jira.auditing.category"),
    USER_MANAGEMENT("user management", "jira.auditing.category.usermanagement"),
    GROUP_MANAGEMENT("group management", "jira.auditing.category.groupmanagement"),
    PERMISSIONS("permissions", "jira.auditing.category.permissions"),
    WORKFLOWS("workflows", "jira.auditing.category.workflows"),
    NOTIFICATIONS("notifications", "jira.auditing.category.notifications"),
    FIELDS("fields", "jira.auditing.category.fields"),
    PROJECTS("projects", "jira.auditing.category.projects"),
    SYSTEM("system","jira.auditing.category.system");

    private final String id;
    private final String nameI18nKey;

    private AuditingCategory(final String id, final String nameI18nKey)
    {
        this.id = id;
        this.nameI18nKey = nameI18nKey;
    }

    public String getId()
    {
        return id;
    }

    public String getNameI18nKey()
    {
        return nameI18nKey;
    }

    @Nullable
    public static AuditingCategory getCategoryById(String categoryId)
    {
        for (AuditingCategory category : values())
        {
            if (category.getId().equals(categoryId)) return category;
        }
        return null;
    }
}
