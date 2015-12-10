package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.user.UserFilterUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import java.util.Collection;

/**
 * A {@link FieldConfigItemType} that represents user filter for the user picker.
 *
 * @since v6.2
 */
public class UserFilterConfigItem implements FieldConfigItemType
{
    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final UserFilterManager userFilterManager;

    public UserFilterConfigItem(final GroupManager groupManager,
            final ProjectRoleManager projectRoleManager,
            final SoyTemplateRenderer soyTemplateRenderer,
            final UserFilterManager userFilterManager)
    {
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.userFilterManager = userFilterManager;
    }

    @Override
    public String getDisplayName()
    {
        return "User Filtering";
    }

    @Override
    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.userpickerfilter";
    }

    @Override
    public String getViewHtml(final FieldConfig fieldConfig, final FieldLayoutItem fieldLayoutItem)
    {
        try
        {
            final Collection<Group> allGroups = groupManager.getAllGroups();
            final UserFilter filter = userFilterManager.getFilter(fieldConfig);
            return soyTemplateRenderer.render("jira.webresources:user-picker-filter-configuration-soy-templates",
                    "JIRA.Templates.Admin.CustomFields.UserPickerFilter.showConfigSummary",
                    MapBuilder.<String, Object>build(
                            "filter", filter,
                            "groups", UserFilterUtils.sortGroups(UserFilterUtils.filterRemovedGroups(filter.getGroups(), allGroups)),
                            "projectRoles", UserFilterUtils.getProjectRoles(projectRoleManager, filter.getRoleIds())));
        }
        catch (SoyException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getObjectKey()
    {
        return "userpickerfilter";
    }

    @Override
    public Object getConfigurationObject(final Issue issue, final FieldConfig config)
    {
        return userFilterManager.getFilter(config);
    }

    @Override
    public String getBaseEditUrl()
    {
        return "EditCustomFieldUserPickerFilter.jspa";
    }
}
