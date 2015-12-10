package com.atlassian.jira.cluster;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.atlassian.jira.cluster.distribution.JiraRMICacheManagerPeerProviderFactory;
import com.atlassian.jira.util.JiraUtils;

import com.google.common.annotations.VisibleForTesting;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;

import static com.atlassian.jira.cluster.distribution.JiraRMICacheManagerPeerProviderFactory.PEER_DISCOVERY;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Builder for our EhCacheConfiguration.
 *
 * @since v6.3
 */
public class EhCacheConfigurationFactory
{
    @VisibleForTesting
    public static final String MANAGER_NAME = "JIRA Cache Manager";

    // cacheManagerPeerProviderFactory
    public static final String EHCACHE_MULTICAST_ADDRESS = "ehcache.multicast.address";
    public static final String EHCACHE_MULTICAST_PORT = "ehcache.multicast.port";
    public static final String EHCACHE_MULTICAST_TTL = "ehcache.multicast.timeToLive";
    // cacheManagerPeerListenerFactory
    public static final String EHCACHE_LISTENER_HOSTNAME = "ehcache.listener.hostName";
    public static final String EHCACHE_LISTENER_PORT = "ehcache.listener.port";

    public static final String DEFAULT_MULTICAST_ADDRESS = "230.0.0.1";
    public static final String DEFAULT_MULTICAST_PORT = "4446";
    public static final String DEFAULT_LISTENER_PORT = "40001";
    public static final String EHCACHE_PEER_DISCOVERY = "ehcache.peer.discovery";

    public Configuration newConfiguration(@Nonnull final URL baseXmlConfiguration, @Nonnull final ClusterNodeProperties clusterNodeProperties)
    {
        notNull(baseXmlConfiguration, "baseXmlConfiguration");
        notNull(clusterNodeProperties, "clusterNodeProperties");

        FactoryConfiguration peerListenerFactory = buildPeerListenerFactory(clusterNodeProperties);
        FactoryConfiguration peerProviderFactory = buildPeerProviderFactory(clusterNodeProperties);

        return ConfigurationFactory.parseConfiguration(baseXmlConfiguration)
                .name(MANAGER_NAME)
                .cacheManagerPeerProviderFactory(peerProviderFactory)
                .cacheManagerPeerListenerFactory(peerListenerFactory);
    }

    private FactoryConfiguration buildPeerListenerFactory(final ClusterNodeProperties clusterNodeProperties)
    {
        final String hostname = clusterNodeProperties.getProperty(EHCACHE_LISTENER_HOSTNAME);
        final String port = clusterNodeProperties.getProperty(EHCACHE_LISTENER_PORT);

        final Properties properties = new Properties();

        // We add the JiraUtils to maintain consistency between Java 7 and 8.
        // EhCache uses the default Java 7 approach, and it will fail in the future.
        properties.put("hostName", hostname != null ? hostname : JiraUtils.getHostname());
        properties.put("port", port != null ? port : DEFAULT_LISTENER_PORT);

        return new FactoryConfiguration()
                .className(RMICacheManagerPeerListenerFactory.class.getName())
                .properties(propertiesToString(properties));
    }

    private FactoryConfiguration buildPeerProviderFactory(final ClusterNodeProperties clusterNodeProperties)
    {
        final String address = clusterNodeProperties.getProperty(EHCACHE_MULTICAST_ADDRESS);
        final String port = clusterNodeProperties.getProperty(EHCACHE_MULTICAST_PORT);
        final String timeToLive = clusterNodeProperties.getProperty(EHCACHE_MULTICAST_TTL);
        final String peerDiscovery = clusterNodeProperties.getProperty(EHCACHE_PEER_DISCOVERY);

        final Properties properties = new Properties();

        if (timeToLive != null)
        {
            properties.put("timeToLive", timeToLive);
        }

        properties.put(PEER_DISCOVERY, peerDiscovery != null ? peerDiscovery : "default");
        properties.put("multicastGroupAddress", address != null ? address : DEFAULT_MULTICAST_ADDRESS);
        properties.put("multicastGroupPort", port != null ? port : DEFAULT_MULTICAST_PORT);

        return new FactoryConfiguration()
                .className(JiraRMICacheManagerPeerProviderFactory.class.getName())
                .properties(propertiesToString(properties));
    }

    private String propertiesToString(final Properties properties)
    {
        final Writer out = new StringWriter();
        try
        {
            properties.store(out, null);
        }
        catch (IOException e)
        {
            // Nothing should go wrong with a String Writer
            throw new RuntimeException(e);
        }
        return out.toString();
    }
}
