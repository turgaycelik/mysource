package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

/**
 * A {@link FieldConfigItemType} that represents a default value for the field. 
 */
public final class DefaultValueConfigItem implements FieldConfigItemType
{
    public String getDisplayName()
    {
        return "Default Value";
    }

    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.default.value";
    }

    public String getViewHtml(FieldConfig config, FieldLayoutItem fieldLayoutItem)
    {
        return config.getCustomField().getCustomFieldType().getDescriptor().getDefaultViewHtml(config, fieldLayoutItem);
    }

    public String getObjectKey()
    {
        return "default";
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        return config.getCustomField().getCustomFieldType().getDefaultValue(config);
    }

    public String getBaseEditUrl()
    {
        return "EditCustomFieldDefaults!default.jspa";
    }
}
