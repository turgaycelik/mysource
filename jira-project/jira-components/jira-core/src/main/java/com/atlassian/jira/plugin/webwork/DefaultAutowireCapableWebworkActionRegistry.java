package com.atlassian.jira.plugin.webwork;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.ModuleDescriptor;

public class DefaultAutowireCapableWebworkActionRegistry implements AutowireCapableWebworkActionRegistry
{
    @ClusterSafe
    final ConcurrentMap<String, ModuleDescriptor> registry = new ConcurrentHashMap<String, ModuleDescriptor>();

    public void registerAction(String action, ModuleDescriptor moduleDescriptor)
    {
        Assertions.notNull("action", action);
        Assertions.notNull("moduleDescriptor", moduleDescriptor);

        if(!(moduleDescriptor.getPlugin() instanceof AutowireCapablePlugin))
        {
            throw new IllegalArgumentException("Plugin must be autowire capable in order to be registered: " + moduleDescriptor.getPluginKey());
        }
        registry.putIfAbsent(action, moduleDescriptor);
    }

    public void unregisterPluginModule(ModuleDescriptor moduleDescriptor)
    {
        Assertions.notNull("moduleDescriptor", moduleDescriptor);
        
        for (Iterator<Map.Entry<String, ModuleDescriptor>> iterator = registry.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry<String, ModuleDescriptor> entry = iterator.next();
            //plugins aren't guaranteed to implement equals, so can't just call registry.remove()
            if (moduleDescriptor.getCompleteKey().equals(entry.getValue().getCompleteKey()))
            {
                iterator.remove();
            }
        }
    }

    public AutowireCapablePlugin getPlugin(String action)
    {
        final ModuleDescriptor moduleDescriptor = registry.get(action);
        if(moduleDescriptor != null)
        {
            return (AutowireCapablePlugin) moduleDescriptor.getPlugin();
        }
        return null;
    }

    public boolean containsAction(String action)
    {        
        return registry.containsKey(action);
    }

}
