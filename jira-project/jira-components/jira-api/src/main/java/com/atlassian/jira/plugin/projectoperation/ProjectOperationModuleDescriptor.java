package com.atlassian.jira.plugin.projectoperation;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;

/**
 * A project operation plugin adds extra operations to JIRA's Administer project page.
 * <p/>
 * <b>IMPORTANT NOTE:</b>  This plugin type is only available for internal use.  Please refrain from using
 * this, as the backwards compatibility of this plugin type will NOT be maintained in the future.
 *
 * @since 3.12
 */
public interface ProjectOperationModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableProjectOperation>, OrderableModuleDescriptor
{
    public PluggableProjectOperation getOperation();
}
