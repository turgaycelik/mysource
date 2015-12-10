package com.atlassian.jira.plugin.language;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * Implementation of {@link TranslationTransformModuleDescriptor}.
 *
 * @since v5.1
 */
public class TranslationTransformModuleDescriptorImpl extends AbstractJiraModuleDescriptor<TranslationTransform> implements TranslationTransformModuleDescriptor
{
    private int order;

    public TranslationTransformModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(TranslationTransform.class);
    }

    @Override
    public void disabled()
    {
        super.disabled();
    }

    @Override
    public int getOrder()
    {
        return order;
    }
}
