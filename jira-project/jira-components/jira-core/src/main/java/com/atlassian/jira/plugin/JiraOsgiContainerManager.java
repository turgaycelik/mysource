package com.atlassian.jira.plugin;

import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.osgi.container.OsgiContainerException;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
import com.atlassian.plugin.osgi.container.felix.FelixOsgiContainerManager;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * OSGI container manager that caches service trackers. This is necessary to avoid creating {@code ServiceTracker}
 * instances willy nilly, while at the same time circumventing a memory leak in FelixOsgiContainerManager (JRA-18766).
 */
public class JiraOsgiContainerManager extends FelixOsgiContainerManager implements Startable
{
    /**
     * Logger for JiraOsgiContainerManager.
     */
    private static final Logger log = LoggerFactory.getLogger(JiraOsgiContainerManager.class);

    /**
     * A cache of service trackers.
     */
    @ClusterSafe
    private final Cache<String, CacheObject<ServiceTracker>> serviceTrackerCache;

    public JiraOsgiContainerManager(final PluginPath pluginPath, final PackageScannerConfiguration packageScannerConfig, final HostComponentProvider provider, final PluginEventManager eventManager)
    {
        super(pluginPath.getOsgiPersistentCache(), packageScannerConfig, provider, eventManager);
        serviceTrackerCache = CacheBuilder.newBuilder()
                .expireAfterAccess(15, MINUTES)
                .removalListener(new ServiceTrackerRemovalListener())
                .build(new ServiceTrackerLoader());
    }

    /**
     * Retrieves and returns a public component from OSGi land via its class name.  This method can be used to retrieve
     * a component provided via a plugins2 OSGi bundle.  Please note that components returned via this method <b>should
     * NEVER be cached</b> (e.g. in a field) as they may be refreshed at any time as a plugin is enabled/disabled or the
     * ComponentManager is reinitialised (after an XML import).
     * <p/>
     * Added as part of the fix for JRADEV-6195.
     *
     * @param clazz The interface class as a String
     * @return the component, or null if not found
     * @since 5.1
     */
    public <T> T getOsgiComponentOfType(final Class<T> clazz)
    {
        if (isRunning())
        {
            ServiceTracker serviceTracker = getServiceTrackerFromCache(clazz.getName());
            if (serviceTracker != null)
            {
                return clazz.cast(serviceTracker.getService());
            }
        }

        return null;
    }

    /**
     * Registers the {@code serviceTrackers} cache in JIRA instrumentation.
     */
    public void start()
    {
        new GoogleCacheInstruments(JiraOsgiContainerManager.class.getSimpleName()).addCache(serviceTrackerCache).install();
        super.start();
    }

    @Override
    public void stop() throws OsgiContainerException
    {
        serviceTrackerCache.invalidateAll();
        super.stop();
    }

    /**
     * Returns a cached service tracker. Note that the {@code ServiceTracker} instance may be closed at any time after
     * it has been returned, so <b>it is not advisable to hold a reference to it</b> (e.g. in a field).
     *
     * @param className a String containing the class name
     * @return a ServiceTracker, or null
     */
    private ServiceTracker getServiceTrackerFromCache(String className)
    {
        return serviceTrackerCache.getUnchecked(className).getValue();
    }

    /**
     * Loads service trackers from the OsgiContainerManager.
     */
    private class ServiceTrackerLoader extends CacheLoader<String, CacheObject<ServiceTracker>>
    {
        @Override
        public CacheObject<ServiceTracker> load(String className) throws Exception
        {
            ServiceTracker serviceTracker = getServiceTracker(className);
            log.trace("Created service tracker for '{}': {}", className, serviceTracker);

            return CacheObject.wrap(serviceTracker);
        }
    }

    /**
     * Closes service trackers when they are evicted from the cache.
     */
    private static class ServiceTrackerRemovalListener implements RemovalListener<String, CacheObject<ServiceTracker>>
    {
        @Override
        public void onRemoval(RemovalNotification<String, CacheObject<ServiceTracker>> notification)
        {
            CacheObject<ServiceTracker> cacheObject = notification.getValue();
            if (cacheObject != null)
            {
                ServiceTracker serviceTracker = cacheObject.getValue();
                if (serviceTracker != null)
                {
                    log.trace("Closing service tracker: {}", serviceTracker);
                    serviceTracker.close();
                }
            }
        }
    }
}
