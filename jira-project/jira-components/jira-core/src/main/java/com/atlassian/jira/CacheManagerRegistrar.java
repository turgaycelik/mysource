package com.atlassian.jira;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.MBeanServer;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.ehcache.EhCacheManager;
import com.atlassian.cache.impl.jmx.MBeanRegistrar;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.cache.CacheCompactor;
import com.atlassian.jira.cache.EhCacheCompactor;
import com.atlassian.jira.cache.NullCacheCompactor;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.cluster.EhCacheConfigurationFactory;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.config.Configuration;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Registers the {@link CacheManager} with the Pico container.
 *
 * @since 6.2
 */
class CacheManagerRegistrar
{
    /**
     * The name of the system property which if set provides the URL of the
     * Ehcache XML configuration file (overriding the built-in ehcache.xml
     * file). If this URL begins with "/", it will be resolved relative to the
     * classpath.
     */
    public static final String EHCACHE_CONFIGURATION = "atlassian.ehcache.config";

    /**
     * The name of the system property which if set to "true" enables JMX
     * monitoring of atlassian-cache (which MBeans are available depends upon
     * the atlassian-cache implementation).
     */
    public static final String ENABLE_JMX = "atlassian.cache.jmx";

    /**
     * The name of the system property which if set to "true" forces the use of
     * Ehcache.
     */
    public static final String FORCE_EHCACHE = "atlassian.cache.ehcache";

    // The scope under which the CacheManager is registered
    @VisibleForTesting
    static final ComponentContainer.Scope MANAGER_SCOPE = PROVIDED;
    static final ComponentContainer.Scope COMPACTOR_SCOPE = INTERNAL;

    // The key under which the CacheManager is registered
    @VisibleForTesting
    static final Class<CacheManager> MANAGER_KEY = CacheManager.class;
    static final Class<CacheCompactor> COMPACTOR_KEY = CacheCompactor.class;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerRegistrar.class);

    /**
     * Registers the CacheManager with the given container.
     *
     * @param container the container to receive the CacheManager (required)
     * @param mBeanServer the JMX server with which to register any MBeans (ignored if null)
     */
    public static void registerCacheManager(final ComponentContainer container, final MBeanServer mBeanServer)
    {
        final JiraProperties jiraProperties = JiraSystemProperties.getInstance();
        final ClusterNodeProperties clusterNodeProperties = container.getComponentInstance(ClusterNodeProperties.class);
        final boolean useEhcache = useEhcache(jiraProperties, clusterNodeProperties);
        LOGGER.debug("Using Ehcache = {}", useEhcache);
        final CacheManager cacheManager;
        final CacheCompactor cacheCompactor;
        if (useEhcache)
        {
            net.sf.ehcache.CacheManager delegate = getCacheManagerDelegate(jiraProperties, clusterNodeProperties);
            cacheManager = new EhCacheManager(delegate, null);
            cacheCompactor = new EhCacheCompactor(delegate);
        }
        else
        {
            cacheManager = new MemoryCacheManager();
            cacheCompactor = new NullCacheCompactor();
        }
        enableJmxIfNecessary(jiraProperties, cacheManager, mBeanServer);
        container.instance(MANAGER_SCOPE, MANAGER_KEY, cacheManager);
        container.instance(COMPACTOR_SCOPE, COMPACTOR_KEY, cacheCompactor);
    }

    private static net.sf.ehcache.CacheManager getCacheManagerDelegate(final JiraProperties jiraProperties,
            final ClusterNodeProperties clusterNodeProperties)
    {
        URL configUrl = CacheManagerRegistrar.class.getResource("/ehcache.xml");
        final String customEhcacheConfig = jiraProperties.getProperty(EHCACHE_CONFIGURATION);
        if (isNotBlank(customEhcacheConfig))
        {
            // The user has specified a custom Ehcache configuration; apply it
            final File customEhcacheConfigFile = new File(customEhcacheConfig);
            if (customEhcacheConfigFile.isFile())
            {
                try
                {
                    configUrl = customEhcacheConfigFile.toURI().toURL();
                }
                catch (final MalformedURLException e)
                {
                    throw new IllegalStateException("Could not create a URL from " + customEhcacheConfigFile);
                }
            }
            else
            {
                LOGGER.error(customEhcacheConfigFile + " is not a file; defaulting to JIRA's built-in Ehcache configuration");
            }
        }

        Configuration config = buildConfiguration(configUrl, clusterNodeProperties);

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    private static Configuration buildConfiguration(final URL configUrl, final ClusterNodeProperties clusterNodeProperties)
    {
        final EhCacheConfigurationFactory configurationFactory = new EhCacheConfigurationFactory();
        return configurationFactory.newConfiguration(configUrl, clusterNodeProperties);
    }

    private static void enableJmxIfNecessary(
            final JiraProperties jiraProperties, final CacheManager cacheManager, final MBeanServer mBeanServer)
    {
        if (jiraProperties.getBoolean(ENABLE_JMX))
        {
            if (cacheManager instanceof MBeanRegistrar)
            {
                ((MBeanRegistrar) cacheManager).registerMBeans(mBeanServer);
            }
        }
    }

    private static boolean useEhcache(final JiraProperties jiraProperties, final ClusterNodeProperties clusterNodeProperties)
    {
        final boolean isEhCacheForced = jiraProperties.getBoolean(FORCE_EHCACHE);
        // Ideally we would invoke ClusterManager#isClustered(), however this causes a circular dependency
        final boolean isJiraClustered = clusterNodeProperties.getNodeId() != null;
        return isEhCacheForced || isJiraClustered;
    }
}
