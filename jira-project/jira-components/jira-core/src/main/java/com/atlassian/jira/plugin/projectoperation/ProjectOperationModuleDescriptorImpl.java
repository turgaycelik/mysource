package com.atlassian.jira.plugin.projectoperation;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * A project operation plugin adds extra operations to JIRA's Administer project page.
 * <p/>
 * <b>IMPORTANT NOTE:</b>  This plugin type is only available for internal use.  Please refrain from using
 * this, as the backwards compatibility of this plugin type will NOT be maintained in the future.
 *
 * @since 3.12
 */
public class ProjectOperationModuleDescriptorImpl extends AbstractJiraModuleDescriptor<PluggableProjectOperation> implements ProjectOperationModuleDescriptor
{
    private int order;

    public ProjectOperationModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        assertResourceExists("velocity", "view");

        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(PluggableProjectOperation.class);
    }

    public PluggableProjectOperation getOperation()
    {
        return getModule();
    }

    public int getOrder()
    {
        return order;
    }
}
