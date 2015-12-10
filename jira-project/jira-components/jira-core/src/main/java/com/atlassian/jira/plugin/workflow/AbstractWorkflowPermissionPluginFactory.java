package com.atlassian.jira.plugin.workflow;

import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissionCategory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;

import static com.atlassian.jira.permission.ProjectPermissionCategory.ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.ISSUES;
import static com.atlassian.jira.permission.ProjectPermissionCategory.OTHER;
import static com.atlassian.jira.permission.ProjectPermissionCategory.PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissionCategory.TIME_TRACKING;
import static com.atlassian.jira.permission.ProjectPermissionCategory.VOTERS_AND_WATCHERS;
import static com.atlassian.jira.util.collect.MapBuilder.singletonMap;
import static com.atlassian.jira.workflow.WorkflowDescriptorUtil.resolvePermissionKey;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.unmodifiableMap;

/**
 * A common base class for Workflow Plugin Factories that are concerned with Permissions.
 *
 * @since v3.13
 */
public abstract class AbstractWorkflowPermissionPluginFactory extends AbstractWorkflowPluginFactory
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    protected AbstractWorkflowPermissionPluginFactory(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> params)
    {
        String permission = extractSingleParam(params, "permissionKey");
        return ImmutableMap.of("permissionKey", permission);
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams)
    {
        populateTemplateParamsForInputAndEdit(velocityParams, null);
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor)
    {
        populateTemplateParamsForInputAndEdit(velocityParams, extractArgs(descriptor));
    }

    protected void populateTemplateParamsForInputAndEdit(Map<String, Object> velocityParams, Map<?, ?> descriptorArgs)
    {
        ProjectPermissionKey permissionKey = null;
        if (descriptorArgs != null)
        {
            permissionKey = resolvePermissionKey(descriptorArgs);
            velocityParams.put("permission", permissionKey.permissionKey());
        }

        velocityParams.put("permissions", unmodifiableMap(getGroupedPermissions(permissionKey)));
    }

    /**
     * JRA-14306: building a map of maps, so we can use optgroups to display each group of permissions.
     *
     * @return a Map with keys as the i18n key of the group, and the value as the map of permissions for that group.
     */
    private Map<String, Map<String, String>> getGroupedPermissions(ProjectPermissionKey selectedKey)
    {
        I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        Map<String, Map<String, String>> groups = newLinkedHashMap();

        groups.put("admin.permission.group.project.permissions", permissionsWithCategory(PROJECTS, i18nHelper));
        groups.put("admin.permission.group.issue.permissions", permissionsWithCategory(ISSUES, i18nHelper));
        groups.put("admin.permission.group.voters.and.watchers.permissions", permissionsWithCategory(VOTERS_AND_WATCHERS, i18nHelper));
        groups.put("admin.permission.group.comments.permissions", permissionsWithCategory(COMMENTS, i18nHelper));
        groups.put("admin.permission.group.attachments.permissions", permissionsWithCategory(ATTACHMENTS, i18nHelper));
        groups.put("admin.permission.group.time.tracking.permissions", permissionsWithCategory(TIME_TRACKING, i18nHelper));

        Map<String, String> otherPermissions = permissionsWithCategory(OTHER, i18nHelper);
        if (!otherPermissions.isEmpty())
        {
            groups.put("admin.permission.group.other.permissions", otherPermissions);
        }

        if (selectedKey != null && !permissionsContainSelected(groups, selectedKey))
        {
            groups.put("admin.permission.group.unavailable.permissions",
                singletonMap(selectedKey.permissionKey(), selectedKey.permissionKey()));
        }

        return groups;
    }

    private Map<String, String> permissionsWithCategory(ProjectPermissionCategory category, I18nHelper i18nHelper)
    {
        Map<String, String> keysToPermissionNames = newLinkedHashMap();
        for (ProjectPermission permission : permissionManager.getProjectPermissions(category))
        {
            String permissionName = i18nHelper.getText(permission.getNameI18nKey());
            keysToPermissionNames.put(permission.getKey(), permissionName);
        }
        return keysToPermissionNames;
    }

    private boolean permissionsContainSelected(Map<String, Map<String, String>> groups, ProjectPermissionKey selectedKey)
    {
        for (Map<String, String> permissions : groups.values())
        {
            if (permissions.containsKey(selectedKey.permissionKey()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor)
    {
        populateTemplateParamsForView(velocityParams, extractArgs(descriptor));
    }

    protected void populateTemplateParamsForView(Map<String, Object> velocityParams, Map<?, ?> descriptorArgs)
    {
        ProjectPermissionKey permissionKey = resolvePermissionKey(descriptorArgs);
        Option<ProjectPermission> permission = permissionManager.getProjectPermission(permissionKey);
        if (permission.isDefined())
        {
            velocityParams.put("permission", permission.get().getNameI18nKey());
            velocityParams.put("defined", true);
        }
        else
        {
            velocityParams.put("permission", permissionKey.permissionKey());
            velocityParams.put("defined", false);
        }
    }

    protected abstract Map<?, ?> extractArgs(AbstractDescriptor descriptor);

    protected void clearLegacyPermissionArgument(Map<?, ?> descriptorArgs)
    {
        if (descriptorArgs.containsKey("permission"))
        {
            descriptorArgs.remove("permission");
        }
    }
}
