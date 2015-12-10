package com.atlassian.jira.issue.fields.config;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

public final class FieldConfigItemImpl implements FieldConfigItem
{
    private final FieldConfigItemType type;
    private final FieldConfig fieldConfig;

    public FieldConfigItemImpl(final FieldConfigItemType type, final FieldConfig config)
    {
        this.type = type;
        this.fieldConfig = config;
    }

    public String getDisplayName()
    {
        return getType().getDisplayName();
    }

    public String getDisplayNameKey()
    {
        return getType().getDisplayNameKey();
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem)
    {
        return getType().getViewHtml(getFieldConfig(), fieldLayoutItem);
    }

    public String getBaseEditUrl()
    {
        return getType().getBaseEditUrl();
    }

    public String getObjectKey()
    {
        return getType().getObjectKey();
    }

    public Object getConfigurationObject(Issue issue)
    {
        return getType().getConfigurationObject(issue, getFieldConfig());
    }

    public FieldConfigItemType getType()
    {
        return type;
    }

    public FieldConfig getFieldConfig()
    {
        return fieldConfig;
    }
}
