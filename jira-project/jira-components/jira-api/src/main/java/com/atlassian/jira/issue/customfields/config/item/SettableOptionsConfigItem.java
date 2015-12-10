package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

public class SettableOptionsConfigItem implements FieldConfigItemType
{
    private final CustomFieldType customFieldType;
    private final OptionsManager optionsManager;

    public SettableOptionsConfigItem(CustomFieldType customFieldType, OptionsManager optionsManager)
    {
        this.customFieldType = customFieldType;
        this.optionsManager = optionsManager;
    }

    public String getDisplayName()
    {
        return "Options";
    }

    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.options";
    }

    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        final Options options = optionsManager.getOptions(fieldConfig);
        return CustomFieldUtils.prettyPrintOptions(options);
    }

    public String getObjectKey()
    {
        return "options";
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        return optionsManager.getOptions(config);
    }

    public String getBaseEditUrl()
    {
        return "EditCustomFieldOptions!default.jspa";
    }
}
