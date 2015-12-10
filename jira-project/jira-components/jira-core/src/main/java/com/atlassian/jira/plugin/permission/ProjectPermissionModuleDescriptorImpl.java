package com.atlassian.jira.plugin.permission;

import java.util.Collection;

import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;

import com.google.common.base.Joiner;

import org.dom4j.Element;

import static com.atlassian.jira.permission.ProjectPermissionCategory.values;
import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.ASSIGNABLE_USER;
import static com.atlassian.jira.permission.ProjectPermissions.ASSIGN_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.CLOSE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.LINK_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.MANAGE_WATCHERS;
import static com.atlassian.jira.permission.ProjectPermissions.MODIFY_REPORTER;
import static com.atlassian.jira.permission.ProjectPermissions.MOVE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.RESOLVE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.SCHEDULE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.SET_ISSUE_SECURITY;
import static com.atlassian.jira.permission.ProjectPermissions.TRANSITION_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.VIEW_READONLY_WORKFLOW;
import static com.atlassian.jira.permission.ProjectPermissions.VIEW_VOTERS_AND_WATCHERS;
import static com.atlassian.jira.permission.ProjectPermissions.WORK_ON_ISSUES;
import static com.atlassian.plugin.util.validation.ValidationPattern.test;
import static java.util.Arrays.asList;

/**
 * Descriptor definition for the project permission plugin point.
 *
 * @since v6.3
 */
public class ProjectPermissionModuleDescriptorImpl extends AbstractJiraModuleDescriptor<ProjectPermission>
        implements ProjectPermissionModuleDescriptor
{
    private static final String SYSTEM_PLUGIN_KEY = "jira.system.project.permissions";

    private String descriptionI18nKey;
    private ProjectPermissionCategory category;

    public ProjectPermissionModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        if (systemPermissionOverride(plugin))
        {
            throw new PluginParseException("Plugin '" + plugin.getKey() + "' cannot override system project permission '" + getKey() + "'");
        }

        descriptionI18nKey = element.attributeValue("i18n-description-key");

        initCategory(element);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern.rule(test("@i18n-name-key").withError("The 'i18n-name-key' attribute is required for project permission module descriptors"));
        pattern.rule(test("@category").withError("The 'category' attribute is required for project permission module descriptors"));
    }

    private boolean systemPermissionOverride(Plugin plugin)
    {
        return !SYSTEM_PLUGIN_KEY.equals(plugin.getKey()) && systemProjectPermissionKeys().contains(new ProjectPermissionKey(getKey()));
    }

    public static Collection<ProjectPermissionKey> systemProjectPermissionKeys()
    {
        return asList(
            ADMINISTER_PROJECTS, BROWSE_PROJECTS, VIEW_READONLY_WORKFLOW,
            CREATE_ISSUES, EDIT_ISSUES, TRANSITION_ISSUES, SCHEDULE_ISSUES,
            MOVE_ISSUES, ASSIGN_ISSUES, ASSIGNABLE_USER, RESOLVE_ISSUES,
            CLOSE_ISSUES, MODIFY_REPORTER, DELETE_ISSUES, LINK_ISSUES,
            SET_ISSUE_SECURITY, VIEW_VOTERS_AND_WATCHERS, MANAGE_WATCHERS, ADD_COMMENTS,
            EDIT_ALL_COMMENTS, EDIT_OWN_COMMENTS, DELETE_ALL_COMMENTS, DELETE_OWN_COMMENTS,
            CREATE_ATTACHMENTS, DELETE_ALL_ATTACHMENTS, DELETE_OWN_ATTACHMENTS, WORK_ON_ISSUES,
            EDIT_OWN_WORKLOGS, EDIT_ALL_WORKLOGS, DELETE_OWN_WORKLOGS, DELETE_ALL_WORKLOGS
        );
    }

    private void initCategory(Element element) throws PluginParseException
    {
        String categoryKey = element.attributeValue("category");

        for (ProjectPermissionCategory category : ProjectPermissionCategory.values())
        {
            if (category.name().equalsIgnoreCase(categoryKey))
            {
                this.category = category;
                return;
            }
        }

        throw new PluginParseException("Invalid project permission category key '" + categoryKey +
                "'. Allowed values: " + Joiner.on(", ").join(values()));
    }

    @Override
    protected ProjectPermission createModule()
    {
        return new DefaultProjectPermission(getKey(), getI18nNameKey(), descriptionI18nKey, category);
    }
}
