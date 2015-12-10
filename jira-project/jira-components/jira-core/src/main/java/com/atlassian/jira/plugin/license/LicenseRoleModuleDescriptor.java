package com.atlassian.jira.plugin.license;

import com.atlassian.jira.license.LicenseRoleDefinition;
import com.atlassian.jira.license.LicenseRoleDefinitionImpl;
import com.atlassian.plugin.ModuleDescriptor;

/**
 * A module descriptor allowing plugins to declare license roles.
 */
public interface LicenseRoleModuleDescriptor extends ModuleDescriptor<LicenseRoleDefinition>
{
}
