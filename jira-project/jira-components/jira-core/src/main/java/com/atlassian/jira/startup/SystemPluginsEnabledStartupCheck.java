package com.atlassian.jira.startup;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.metadata.PluginMetadataManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.join;

/**
 * Checks that all system plugins have been enabled.
 *
 * @since 4.3.1
 */
public class SystemPluginsEnabledStartupCheck implements StartupCheck
{
    /**
     * The fault description text.
     */
    private static final String FAULT_DESC_TEXT = "The following plugins are required by JIRA, but have not been started:";

    /**
     * Logger for this SystemPluginsEnabledStartupCheck.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPluginsEnabledStartupCheck.class);

    /**
     * A reference to the list of system plugins that are not enabled.
     */
    private final AtomicReference<ImmutableList<PluginInfo>> disabledSystemPluginKeys = new AtomicReference<ImmutableList<PluginInfo>>(null);

    /**
     * Creates a new SystemPluginsEnabledStartupCheck.
     */
    public SystemPluginsEnabledStartupCheck()
    {
    }

    /**
     * Returns true if all system plugins were enabled at the time that this check was first performed.
     *
     * @return a boolean indicating if all system plugins are enabled
     */
    public boolean isOk()
    {
        return disabledSystemPluginKeys().isEmpty();
    }

    public String getName()
    {
        return "System Plugins Startup Check";
    }

    public String getFaultDescription()
    {
        return String.format(FAULT_DESC_TEXT + " %s", join(disabledSystemPluginKeys(), ", "));
    }

    public String getHTMLFaultDescription()
    {
        StringBuilder desc = new StringBuilder();
        desc.append("<p>" + FAULT_DESC_TEXT + "</p>");
        desc.append("<ul>");
        for (PluginInfo pluginInfo : disabledSystemPluginKeys())
        {
            desc.append("<li>").append(escapeHtml(pluginInfo.toString())).append("</li>");
        }
        desc.append("</ul>");

        return desc.toString();
    }

    @Override
    public void stop()
    {
    }

    /**
     * Returns the list of disabled system plugins (lazily instantiated).
     *
     * @return a List<PluginInfo> containing disabled system plugins
     */
    private ImmutableList<PluginInfo> disabledSystemPluginKeys()
    {
        ImmutableList<PluginInfo> disabledKeys = disabledSystemPluginKeys.get();
        if (disabledKeys == null)
        {
            ImmutableList<PluginInfo> newList = calculateDisabledSystemPlugins();

            // use newList if we won the race. otherwise get the value that did win
            boolean wasSet = disabledSystemPluginKeys.compareAndSet(null, newList);
            disabledKeys = wasSet ? newList : disabledSystemPluginKeys.get();
        }

        return disabledKeys;
    }

    /**
     * Returns a list containing the system (i.e. non-optional) plugins that are not currently enabled.
     *
     * @return an ImmutableList of PluginInfo
     */
    protected ImmutableList<PluginInfo> calculateDisabledSystemPlugins()
    {
        ComponentManager componentManager = ComponentManager.getInstance();
        if (componentManager == null)
        {
            throw new IllegalStateException("ComponentManager is null");
        }

        PluginAccessor pluginAccessor = componentManager.getPluginAccessor();
        if (pluginAccessor == null)
        {
            throw new IllegalStateException("PluginAccessor is null");
        }

        PluginMetadataManager pluginMetadataManager = (PluginMetadataManager) componentManager.getContainer().getComponent(PluginMetadataManager.class);
        if (pluginMetadataManager == null)
        {
            throw new IllegalStateException("PluginMetadataManager is null");
        }

        Collection<Plugin> allPlugins = pluginAccessor.getPlugins();
        LOGGER.trace("All plugins: {}", allPlugins);

        Collection<Plugin> systemPlugins = filter(allPlugins, new SystemPluginPredicate(pluginMetadataManager));
        LOGGER.trace("System plugins: {}", systemPlugins);

        Collection<Plugin> disabledSystemPlugins = filter(systemPlugins, new PluginNotEnabledPredicate());
        LOGGER.trace("Disabled system plugins: {}", disabledSystemPlugins);

        return ImmutableList.copyOf(transform(disabledSystemPlugins, new ExtractPluginInfoFunction()));
    }

    /**
     * Predicate that selects system plugins.
     */
    private static class SystemPluginPredicate implements Predicate<Plugin>
    {
        private final PluginMetadataManager pluginMetadataManager;

        public SystemPluginPredicate(PluginMetadataManager pluginMetadataManager)
        {
            this.pluginMetadataManager = pluginMetadataManager;
        }

        @Override
        public boolean apply(Plugin plugin)
        {
            return !pluginMetadataManager.isOptional(plugin);
        }
    }

    /**
     * Predicate that is is used to determine if a plugin is not enabled.
     */
    private class PluginNotEnabledPredicate implements Predicate<Plugin>
    {
        public boolean apply(Plugin input)
        {
            return !PluginState.ENABLED.equals(input.getPluginState());
        }
    }

    /**
     * Function that is used to extract plugin info from the plugin.
     */
    private static class ExtractPluginInfoFunction implements Function<Plugin, PluginInfo>
    {
        public PluginInfo apply(Plugin plugin)
        {
            return new PluginInfo(plugin.getKey(), plugin.getName());
        }
    }

    /**
     * Utility class for storing plugin info. Java why can't this class just be a two-liner!???
     */
    static class PluginInfo
    {
        final String key;
        final String name;

        PluginInfo(String key, String name)
        {
            this.key = Assertions.notNull(key);
            this.name = Assertions.notNull(name);
        }

        @Override
        public String toString()
        {
            return name + " (" + key + ")";
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            PluginInfo that = (PluginInfo) o;

            if (!key.equals(that.key)) { return false; }
            if (!name.equals(that.name)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = key.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
