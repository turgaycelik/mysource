package com.atlassian.jira.plugin.renderercomponent;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Looks for renderer components that allow plugins to add new wiki renderer components to the render chain
 *
 * @since 3.12
 */
public class RendererComponentFactoryDescriptor extends AbstractJiraModuleDescriptor<PluggableRendererComponentFactory>
        implements OrderableModuleDescriptor
{
    private int order = 0;
    private Map<String, List<String>> listParams = new HashMap<String, List<String>>();

    public RendererComponentFactoryDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        //read extra list of params if they exist
        final Element paramListElement = element.element("list-param");
        if (paramListElement != null)
        {
            @SuppressWarnings ({ "unchecked" })
            List<Element> valueElements = paramListElement.elements("value");
            if (valueElements != null && !valueElements.isEmpty())
            {
                List<String> paramList = new ArrayList<String>();
                for (final Element valueElement : valueElements)
                {
                    paramList.add(valueElement.getTextTrim());
                }
                listParams.put(paramListElement.attribute("name").getText(), paramList);
            }
        }

        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(PluggableRendererComponentFactory.class);
    }

    public Map<String, List<String>> getListParams()
    {
        return listParams;
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
