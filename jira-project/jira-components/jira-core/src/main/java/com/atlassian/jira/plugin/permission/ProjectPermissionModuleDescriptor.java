package com.atlassian.jira.plugin.permission;

import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.plugin.ModuleDescriptor;

/**
 * A module descriptor allowing plugins to declare project permissions.
 *
 * @since v6.3
 */
public interface ProjectPermissionModuleDescriptor extends ModuleDescriptor<ProjectPermission>
{
}
