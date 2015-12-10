package com.atlassian.jira.plugin.contentlinkresolver;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.renderer.links.ContentLinkResolver;
import org.dom4j.Element;

/**
 * Looks for content link resolvers that allow plugins to customise handing of content links
 *
 * @since 3.12
 */
public class ContentLinkResolverDescriptor extends AbstractJiraModuleDescriptor<ContentLinkResolver> implements OrderableModuleDescriptor
{
    private int order = 0;

    public ContentLinkResolverDescriptor(JiraAuthenticationContext jiraAuthenticationContext, final ModuleFactory moduleFactory)
    {
        super(jiraAuthenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(ContentLinkResolver.class);
    }

    public int getOrder()
    {
        return order;
    }

    public boolean hasHelp()
    {
        return getResourceDescriptor("velocity", "help") != null;
    }

    public String getHelpSection()
    {
        return getResourceDescriptor("velocity", "help").getParameter("help-section");
    }

    public String getHelp()
    {
        return getHtml("help");
    }    
}
