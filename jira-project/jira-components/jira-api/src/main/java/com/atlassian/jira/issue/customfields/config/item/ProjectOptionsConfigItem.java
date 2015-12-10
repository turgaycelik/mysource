package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class ProjectOptionsConfigItem implements FieldConfigItemType
{
    private static final Logger log = Logger.getLogger(ProjectOptionsConfigItem.class);

    private final ProjectConverter projectConverter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectOptionsConfigItem(ProjectConverter projectConverter, PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.projectConverter = projectConverter;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return "Project options";
    }

    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.project.options";
    }

    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.issuefields.customfields.config.project.options.all");
    }

    public String getObjectKey()
    {
        return "options";
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        try
        {
            final List<GenericValue> originalList = new ArrayList<GenericValue>(permissionManager.getProjects(Permissions.BROWSE, jiraAuthenticationContext.getLoggedInUser()));
            return new GenericImmutableOptions<GenericValue>(originalList, config);
        }
        catch (UnsupportedOperationException e)
        {
            log.error("Unable to retrieve projects. Likely to be an issue with SubvertedPermissionManager. Please restart to resolve the problem.", e);
            return null;
        }
    }

    public String getBaseEditUrl()
    {
        return null;
    }
}
