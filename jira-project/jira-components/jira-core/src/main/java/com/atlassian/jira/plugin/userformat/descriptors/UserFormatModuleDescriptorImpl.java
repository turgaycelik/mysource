package com.atlassian.jira.plugin.userformat.descriptors;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.LazyReference.InitializationException;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * @since v3.13
 */
public class UserFormatModuleDescriptorImpl extends AbstractJiraModuleDescriptor<UserFormat> implements UserFormatModuleDescriptor
{
    private String type;
    private String typeI18nKey;

    @ClusterSafe("Plugins are kept in synch across the cluster")
    private final ResettableLazyReference<UserFormat> format = new ResettableLazyReference<UserFormat>()
    {
        @SuppressWarnings("deprecation")
        @Override
        protected UserFormat create()
        {
            final Plugin plugin = getPlugin();
            if (plugin instanceof AutowireCapablePlugin)
            {
                return ((AutowireCapablePlugin) plugin).autowire(getModuleClass());
            }
            else
            {
                return JiraUtils.loadComponent(getModuleClass(), CollectionBuilder.<Object> list(UserFormatModuleDescriptorImpl.this));
            }
        }
    };

    public UserFormatModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        final Element typeElement = element.element("type");
        type = typeElement.getTextTrim();
        final Attribute typeI18nAttribute = typeElement.attribute("i18n-name-key");
        if (typeI18nAttribute != null)
        {
            typeI18nKey = typeI18nAttribute.getText();
        }
    }

    @Override
    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(UserFormat.class);
        format.reset();
    }

    @Override
    public void disabled()
    {
        super.disabled();
        format.reset();
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public String getTypeI18nKey()
    {
        return typeI18nKey;
    }

    @Override
    public UserFormat getModule()
    {
        try
        {
            return format.get();
        }
        catch (final InitializationException e)
        {
            format.reset();

            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error)
            {
                throw (Error) cause;
            }
            throw e;
        }
    }

    @Override
    @SuppressWarnings("all")    // The EqualsBuilder checks the type of the given Object
    public boolean equals(final Object obj)
    {
        return new ModuleDescriptors.EqualsBuilder(this).isEqualsTo(obj);
    }

    @Override
    public int hashCode()
    {
        return new ModuleDescriptors.HashCodeBuilder(this).toHashCode();
    }
}
