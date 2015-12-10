package com.atlassian.jira.plugin.index;

import javax.annotation.Nonnull;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.IndexDocumentConfigurationFactory;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Element;

import static com.atlassian.jira.index.IndexDocumentConfigurationFactory.IndexDocumentConfigurationParseException;

/**
 * Descriptor of modules which specify how the properties are indexed. When to module is enabled it is persisted, so the
 * properties are still indexed even if the plugin is disabled. The modules are removed only when the plugin is uninstalled
 * or reinstalled.
 *
 * @since v6.2
 */
public class EntityPropertyIndexDocumentModuleDescriptorImpl extends AbstractJiraModuleDescriptor<Void>
        implements EntityPropertyIndexDocumentModuleDescriptor
{
    private IndexDocumentConfiguration indexDocumentConfiguration;

    public EntityPropertyIndexDocumentModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(@Nonnull final Plugin plugin, @Nonnull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        try
        {
            indexDocumentConfiguration = ComponentAccessor.getComponent(IndexDocumentConfigurationFactory.class).fromXML(element);
        }
        catch (final IndexDocumentConfigurationParseException e)
        {
            throw new PluginParseException("Cannot parse plugin descriptor",e);
        }
    }

    @Override
    public void enabled()
    {
        init();
        super.enabled();
    }

    @Override
    public void init()
    {
        //ComponentManager.getInstance() can be used internally
        //noinspection deprecation
        if (ComponentManager.getInstance().getState().isStarted())
        {
            //we only want to register when ComponentManager was started
            ComponentAccessor.getComponent(PluginIndexConfigurationManager.class).put(getPluginKey(), getKey(), indexDocumentConfiguration);
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
    }

    @Override
    public boolean equals(Object obj)
    {
        return new ModuleDescriptors.EqualsBuilder(this).isEqualsTo(obj);
    }

    @Override
    public int hashCode()
    {
        return new ModuleDescriptors.HashCodeBuilder(this).toHashCode();
    }

    @Override
    public IndexDocumentConfiguration getIndexDocumentConfiguration()
    {
        return indexDocumentConfiguration;
    }
}
