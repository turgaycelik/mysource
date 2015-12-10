package com.atlassian.jira.plugin.ha;



import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * Maps the received message to the appropriate plugin action to take
 *
 * @since v6.1
 */
public class MessageEventRegistry
{
    private final Map<PluginEventType, Function<String, Void>> pluginUpdateMethods = Maps.newEnumMap(PluginEventType.class);

    private final ReplicatedPluginManager replicatedPluginManager;


    public MessageEventRegistry(final ReplicatedPluginManager replicatedPluginManager) {
        this.replicatedPluginManager = replicatedPluginManager;
        initailiseEventMap();
    }

    private void initailiseEventMap( )
    {
        pluginUpdateMethods.put(PluginEventType.PLUGIN_DISABLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.disablePlugin(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_ENABLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.enablePlugin(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_UPGRADED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.upgradePlugin(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_MODULE_DISABLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.disablePluginModule(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_MODULE_ENABLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.enablePluginModule(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_UNINSTALLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.uninstallPlugin(input);
                return null;
            }
        });
        pluginUpdateMethods.put(PluginEventType.PLUGIN_INSTALLED, new Function<String, Void>()
        {
            @Override
            public Void apply(@Nullable final String input)
            {
                replicatedPluginManager.installPlugin(input);
                return null;
            }
        });
    }


    public Function<String, Void> getEventFunction(final PluginEventType pluginEventType)
    {
        return pluginUpdateMethods.get(pluginEventType);
    }
}
