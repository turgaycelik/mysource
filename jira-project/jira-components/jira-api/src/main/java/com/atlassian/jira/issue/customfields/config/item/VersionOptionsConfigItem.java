package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.version.VersionManager;
import org.apache.log4j.Logger;

import java.util.Collections;

public class VersionOptionsConfigItem implements FieldConfigItemType
{
    private static final Logger log = Logger.getLogger(VersionOptionsConfigItem.class);

    private final VersionManager versionManager;

    public VersionOptionsConfigItem(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public String getDisplayName()
    {
        return "Version options";
    }

    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.version.options";
    }

    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        return "All versions available for the project of the issue";
    }

    public String getObjectKey()
    {
        return "options";
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        if (issue != null && issue.getProjectObject() != null)
        {
            return new GenericImmutableOptions(versionManager.getVersions(issue.getProjectObject().getId()), config);
        }
        else
        {
            return new GenericImmutableOptions(Collections.EMPTY_LIST, config);
        }
    }

    public String getBaseEditUrl()
    {
        return null;
    }
}
