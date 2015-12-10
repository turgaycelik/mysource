package com.atlassian.jira.web.action.admin.plugins;

import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Note: not registered in {@link com.atlassian.jira.ContainerRegistrar} because it is only used in one spot, but there's
 * no reason why you couldn't if you wanted to.
 *
 * @since v4.0
 */
public class PluginReindexHelperImpl implements PluginReindexHelper
{
    private static final Logger log = Logger.getLogger(PluginReindexHelperImpl.class);

    private final PluginAccessor pluginAccessor;

    public PluginReindexHelperImpl(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = notNull("pluginAccessor", pluginAccessor);
    }

    public boolean doesEnablingPluginModuleRequireMessage(final String moduleKey)
    {
        final ModuleDescriptor<?> moduleDescriptor = pluginAccessor.getEnabledPluginModule(moduleKey);
        if (moduleDescriptor != null)
        {
            return isDescriptorCustomFieldRelated(moduleDescriptor);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Got a null module descriptor when asking for module '" + moduleKey + "'");
            }
            return false;
        }
    }

    public boolean doesEnablingPluginRequireMessage(final String pluginKey)
    {
        final Plugin plugin = pluginAccessor.getEnabledPlugin(pluginKey);
        if (plugin != null)
        {
            Collection<ModuleDescriptor<?>> moduleDescriptors = plugin.getModuleDescriptors();
            for (ModuleDescriptor<?> moduleDescriptor : moduleDescriptors)
            {
                if (isDescriptorCustomFieldRelated(moduleDescriptor))
                {
                    return true;
                }
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Got a null plugin when asking for key '" + pluginKey + "'");
            }
        }
        return false;
    }

    private boolean isDescriptorCustomFieldRelated(final ModuleDescriptor<?> moduleDescriptor)
    {
        return (moduleDescriptor instanceof CustomFieldTypeModuleDescriptor || moduleDescriptor instanceof CustomFieldSearcherModuleDescriptor);
    }
}
