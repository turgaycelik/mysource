package com.atlassian.jira.plugin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.atlassian.fugue.Option;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.plugin.IllegalPluginStateException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginContainerFailedEvent;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.exception.PluginExceptionInterception;
import com.atlassian.plugin.osgi.event.PluginServiceDependencyWaitEndedEvent;
import com.atlassian.plugin.osgi.event.PluginServiceDependencyWaitStartingEvent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.fugue.Option.option;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Plugins form a big part of what Atlassian software does.  However we don't do a good job in trying to know what
 * plugins are loaded and importantly which ones have failed to load.
 * <p/>
 * This class is used to track what plugins are expected to be loaded, what did load as expected and what failed to
 * load.
 *
 * @since v6.0
 */
public class JiraFailedPluginTracker implements PluginExceptionInterception
{
    private static final Function<Throwable, Option<List<String>>> CAPTURE_CAUSES = new Function<Throwable, Option<List<String>>>()
    {
        @Override
        public Option<List<String>> apply(Throwable input)
        {
            List<String> causes = newArrayList(input.getMessage());
            int indent = 1;
            input = input.getCause();
            while (input != null)
            {
                causes.add(StringUtils.repeat("\t", indent) + input.getMessage());
                input = input.getCause();
                indent++;
            }
            return Option.some(causes);
        }
    };

    @ClusterSafe
    private final ConcurrentMap<String, PluginInfo> failedPlugins = new ConcurrentHashMap<String, PluginInfo>();
    @ClusterSafe
    private final ConcurrentMap<String, PluginInfo> trackedPlugins = new ConcurrentHashMap<String, PluginInfo>();

    public JiraFailedPluginTracker(final PluginEventManager pluginEventManager)
    {
        //
        // this seems technically unsafe in terms of constructor publication but its
        // what the DefaultPluginManager does so....its no worse I guess.  Startable is no good
        // there because its too late.
        //
        pluginEventManager.register(this);
    }


    /**
     * This is called then the PluginFactory has decided it can create a plugin BUT it hasn't started it and hence
     * resolved its dependencies
     *
     * @param plugin the plugin that has been created by the PluginFactory but not resolved
     * @param pluginArtifact the artifact used to load this plugin
     */
    public void trackLoadingPlugin(final Plugin plugin, final PluginArtifact pluginArtifact)
    {
        PluginInfo pluginInfo = new PluginInfo(plugin.getKey(), plugin.getName(), Option.some(pluginArtifact));
        trackedPlugins.put(plugin.getKey(), pluginInfo);
    }

    @PluginEventListener
    public void onPluginEnabledEvent(PluginEnabledEvent enabledEvent)
    {
        String key = enabledEvent.getPlugin().getKey();
        trackedPlugins.remove(key);
        failedPlugins.remove(key);
    }

    @PluginEventListener
    public void onPluginDisabledEvent(PluginDisabledEvent disabledEvent)
    {
        String key = disabledEvent.getPlugin().getKey();
        PluginInfo pluginInfo = trackedPlugins.get(key);
        if (pluginInfo != null)
        {
            // we have a plugin that is gone into disabled state from plugin load.  Maybe that ok but we are going to track it
            // because most likely it has failed to start in time.  We can use hard coded strings here because...well its like the other log messages..is for English speaking
            // support people
            //
            pluginInfo = PluginInfo.addFailure(pluginInfo, "The plugin has been disabled.  A likely cause is that it timed out during initialisation");
            failedPlugins.put(key, pluginInfo);
        }
        trackedPlugins.remove(key);
    }

    @PluginEventListener
    public void onServiceDependencyWaitStarting(PluginServiceDependencyWaitStartingEvent event)
    {
        PluginInfo pluginInfo = trackedPlugins.get(event.getPluginKey());
        if (pluginInfo != null)
        {
            PluginInfo value = PluginInfo.addDependency(pluginInfo, event.getBeanName(), String.valueOf(event.getFilter()));
            trackedPlugins.put(event.getPluginKey(), value);
        }
    }

    @PluginEventListener
    public void onServiceDependencyWaitEnded(PluginServiceDependencyWaitEndedEvent event)
    {
        PluginInfo pluginInfo = trackedPlugins.get(event.getPluginKey());
        if (pluginInfo != null)
        {
            PluginInfo value = PluginInfo.removeDependency(pluginInfo, event.getBeanName());
            trackedPlugins.put(event.getPluginKey(), value);
        }
    }

    /**
     * Called when the plugin container for the bundle has failed to be created.  This means the bundle is still active,
     * but the plugin container is not available, so for our purposes, the plugin shouldn't be enabled.
     *
     * @param event The plugin container failed event
     * @throws com.atlassian.plugin.IllegalPluginStateException If the plugin key hasn't been set yet
     */
    @PluginEventListener
    public void onPluginContainerFailed(final PluginContainerFailedEvent event) throws IllegalPluginStateException
    {
        PluginInfo pluginInfo = trackedPlugins.get(event.getPluginKey());
        if (pluginInfo != null)
        {
            PluginInfo value = PluginInfo.addFailures(pluginInfo, event.getCause());
            failedPlugins.put(event.getPluginKey(), value);
        }
    }

    @Override
    public boolean onEnableException(final Plugin plugin, final Exception pluginException)
    {
        PluginInfo pluginInfo = trackedPlugins.get(plugin.getKey());
        if (pluginInfo != null)
        {
            PluginInfo value = PluginInfo.addFailures(pluginInfo, pluginException);
            failedPlugins.put(plugin.getKey(), value);
        }
        return false;
    }

    /**
     * @return the list if plugins that failed to load
     */
    public Iterable<PluginInfo> getFailedPlugins()
    {
        return newArrayList(failedPlugins.values());
    }

    /**
     * @return the list of plugins that loaded as plugin artifacts but never resolved into actual failed or loaded
     *         plugins.  This list REALLY should be empty once the plugin system has started and the the plugins have
     *         been otherwise we have a serious problem.
     */
    public Iterable<PluginInfo> getUnaccountedForPlugins()
    {
        return newArrayList(Iterables.filter(trackedPlugins.values(), new Predicate<PluginInfo>()
        {
            @Override
            public boolean apply(final PluginInfo input)
            {
                return !failedPlugins.containsKey(input.getPluginKey());
            }
        }));
    }


    /**
     * A simple holder class of plugin information
     */
    public static class PluginInfo
    {

        public static PluginInfo build(final String pluginKey, final String pluginClass, final Option<PluginArtifact> pluginArtifact)
        {
            return new PluginInfo(pluginKey, pluginClass, pluginArtifact);
        }

        public static PluginInfo addFailures(final PluginInfo pluginInfo, final Throwable cause)
        {
            final List<String> copiedCauses = copyPreviousCauses(pluginInfo);
            Option<List<String>> capturedCauses = option(cause).flatMap(CAPTURE_CAUSES);
            if (capturedCauses.isDefined())
            {
                copiedCauses.addAll(capturedCauses.get());
            }

            return new PluginInfo(pluginInfo.pluginKey, pluginInfo.pluginName, pluginInfo.pluginArtifact, option(copiedCauses), pluginInfo.dependencies);
        }

        public static PluginInfo addFailure(final PluginInfo pluginInfo, final String cause)
        {
            final List<String> copiedCauses = copyPreviousCauses(pluginInfo);
            copiedCauses.add(cause);

            return new PluginInfo(pluginInfo.pluginKey, pluginInfo.pluginName, pluginInfo.pluginArtifact, option(copiedCauses), pluginInfo.dependencies);
        }

        private static List<String> copyPreviousCauses(final PluginInfo pluginInfo)
        {
            final List<String> copiedCauses = Lists.newArrayList();
            if (pluginInfo.getFailureCauses().isDefined())
            {
                copiedCauses.addAll(pluginInfo.getFailureCauses().get());
            }
            return copiedCauses;
        }

        public static PluginInfo addDependency(final PluginInfo pluginInfo, String serviceName, String serviceClass)
        {
            List<ServiceDependency> deps = newArrayList(pluginInfo.getDependencies());
            deps.add(new ServiceDependency(serviceName, serviceClass));
            return new PluginInfo(pluginInfo, deps);
        }

        public static PluginInfo removeDependency(final PluginInfo pluginInfo, final String serviceName)
        {
            Predicate<ServiceDependency> NOT_SERVICE_PREDICATE = new Predicate<ServiceDependency>()
            {
                @Override
                public boolean apply(final ServiceDependency input)
                {
                    return !input.getServiceName().equals(serviceName);
                }
            };
            List<ServiceDependency> deps = newArrayList(Iterables.filter(pluginInfo.getDependencies(), NOT_SERVICE_PREDICATE));
            return new PluginInfo(pluginInfo, deps);
        }

        private final String pluginKey;
        private final String pluginName;
        private final Option<PluginArtifact> pluginArtifact;
        private final Option<List<String>> failureCauses;
        private final List<ServiceDependency> dependencies;

        PluginInfo(final String pluginKey, final String pluginName, final Option<PluginArtifact> pluginArtifact)
        {
            this(pluginKey, pluginName, pluginArtifact, Option.<List<String>>none(), Collections.<ServiceDependency>emptyList());
        }

        PluginInfo(PluginInfo copy, List<ServiceDependency> dependencies)
        {
            this(copy.pluginKey, copy.pluginName, copy.pluginArtifact, copy.failureCauses, dependencies);
        }

        PluginInfo(final String pluginKey, final String pluginName, final Option<PluginArtifact> pluginArtifact, final Option<List<String>> failureCauses, List<ServiceDependency> dependencies)
        {
            this.pluginKey = pluginKey;
            this.pluginName = pluginName;
            this.failureCauses = failureCauses;
            this.pluginArtifact = pluginArtifact;
            this.dependencies = dependencies;
        }

        public String getPluginKey()
        {
            return pluginKey;
        }

        public String getPluginName()
        {
            return pluginName;
        }

        public Option<PluginArtifact> getPluginArtifact()
        {
            return pluginArtifact;
        }

        public List<ServiceDependency> getDependencies()
        {
            return newArrayList(dependencies);
        }

        public Option<List<String>> getFailureCauses()
        {
            return failureCauses;
        }
    }

    /**
     * Represents a dependency that a plugin has to some other service
     */
    public static class ServiceDependency
    {
        private final String serviceName;
        private final String serviceClass;


        public ServiceDependency(final String serviceName, final String serviceClass)
        {
            this.serviceName = serviceName;
            this.serviceClass = serviceClass;
        }

        public String getServiceName()
        {
            return serviceName;
        }

        public String getServiceClass()
        {
            return serviceClass;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ServiceDependency that = (ServiceDependency) o;

            return serviceName.equals(that.serviceName) && serviceClass.equals(that.serviceClass);

        }

        @Override
        public int hashCode()
        {
            int result = serviceName.hashCode();
            result = 31 * result + serviceClass.hashCode();
            return result;
        }
    }


}
