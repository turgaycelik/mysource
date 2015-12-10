package com.atlassian.jira.cluster;

import java.net.URL;
import java.util.Properties;

import com.atlassian.jira.util.JiraUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.util.PropertyUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestEhCacheConfigurationFactory
{
    @Mock
    private ClusterNodeProperties clusterNodeProperties;
    private static final String MULTICAST_ADDRESS = "201.201.201.1";

    private static final String MULTICAST_PORT = "90001";
    private static final String MULTICAST_TTL = "0";
    private static final String LISTENING_HOSTNAME = "myhost";
    private static final String LISTENING_PORT = "90002";

    @Test
    public void testBuild() throws Exception
    {
        // Use the standard ehcache.xml
        URL configUrl = TestEhCacheConfigurationFactory.class.getResource("/ehcache.xml");

        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_ADDRESS)).thenReturn(MULTICAST_ADDRESS);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_PORT)).thenReturn(MULTICAST_PORT);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_TTL)).thenReturn(MULTICAST_TTL);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn(LISTENING_HOSTNAME);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(LISTENING_PORT);

        final EhCacheConfigurationFactory configurationFactory = new EhCacheConfigurationFactory();
        Configuration config = configurationFactory.newConfiguration(configUrl, clusterNodeProperties);

        String peerProviderProperties = config.getCacheManagerPeerProviderFactoryConfiguration().get(0).getProperties();
        Properties peerProps = PropertyUtil.parseProperties(peerProviderProperties, null);
        assertThat(peerProps.getProperty("peerDiscovery"), is("default"));
        assertThat(peerProps.getProperty("multicastGroupAddress"), is(MULTICAST_ADDRESS));
        assertThat(peerProps.getProperty("multicastGroupPort"), is(MULTICAST_PORT));
        assertThat(peerProps.getProperty("timeToLive"), is("0"));

        String peerListenerProperties = config.getCacheManagerPeerListenerFactoryConfigurations().get(0).getProperties();
        Properties listenerProps = PropertyUtil.parseProperties(peerListenerProperties, null);
        assertThat(listenerProps.getProperty("hostName"), is(LISTENING_HOSTNAME));
        assertThat(listenerProps.getProperty("port"), is(LISTENING_PORT));
        assertThat(listenerProps.getProperty("socketTimeoutMillis"), nullValue());

    }

    @Test
    public void testBuildWithDefaults() throws Exception
    {
        // Use the standard ehcache.xml
        URL configUrl = TestEhCacheConfigurationFactory.class.getResource("/ehcache.xml");

        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_ADDRESS)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_PORT)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_TTL)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(null);

        final EhCacheConfigurationFactory configurationFactory = new EhCacheConfigurationFactory();
        Configuration config = configurationFactory.newConfiguration(configUrl, clusterNodeProperties);

        String peerProviderProperties = config.getCacheManagerPeerProviderFactoryConfiguration().get(0).getProperties();
        Properties peerProps = PropertyUtil.parseProperties(peerProviderProperties, null);
        assertThat(peerProps.getProperty("peerDiscovery"), is("default"));
        assertThat(peerProps.getProperty("multicastGroupAddress"), is("230.0.0.1"));
        assertThat(peerProps.getProperty("multicastGroupPort"), is("4446"));
        assertThat(peerProps.getProperty("timeToLive"), nullValue());

        String peerListenerProperties = config.getCacheManagerPeerListenerFactoryConfigurations().get(0).getProperties();
        Properties listenerProps = PropertyUtil.parseProperties(peerListenerProperties, null);
        assertThat(listenerProps.getProperty("hostName"), is(JiraUtils.getHostname()));
        assertThat(listenerProps.getProperty("port"), is(EhCacheConfigurationFactory.DEFAULT_LISTENER_PORT));
        assertThat(listenerProps.getProperty("socketTimeoutMillis"), nullValue());

    }

    @Test
    public void testBuildWithMixedDefaults() throws Exception
    {
        // Use the standard ehcache.xml
        URL configUrl = TestEhCacheConfigurationFactory.class.getResource("/ehcache.xml");

        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_ADDRESS)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_PORT)).thenReturn(MULTICAST_PORT);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_MULTICAST_TTL)).thenReturn("10");
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn(null);
        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_PORT)).thenReturn(LISTENING_PORT);

        final EhCacheConfigurationFactory configurationFactory = new EhCacheConfigurationFactory();
        Configuration config = configurationFactory.newConfiguration(configUrl, clusterNodeProperties);

        String peerProviderProperties = config.getCacheManagerPeerProviderFactoryConfiguration().get(0).getProperties();
        Properties peerProps = PropertyUtil.parseProperties(peerProviderProperties, null);
        assertThat(peerProps.getProperty("peerDiscovery"), is("default"));
        assertThat(peerProps.getProperty("multicastGroupAddress"), is("230.0.0.1"));
        assertThat(peerProps.getProperty("multicastGroupPort"), is(MULTICAST_PORT));
        assertThat(peerProps.getProperty("timeToLive"), is("10"));

        String peerListenerProperties = config.getCacheManagerPeerListenerFactoryConfigurations().get(0).getProperties();
        Properties listenerProps = PropertyUtil.parseProperties(peerListenerProperties, null);
        assertThat(listenerProps.getProperty("hostName"), is(JiraUtils.getHostname()));
        assertThat(listenerProps.getProperty("port"), is(LISTENING_PORT));
        assertThat(listenerProps.getProperty("socketTimeoutMillis"), nullValue());
    }

    @Test
    public void testSetHostname()
    {
        // Use the standard ehcache.xml
        URL configUrl = TestEhCacheConfigurationFactory.class.getResource("/ehcache.xml");

        when(clusterNodeProperties.getProperty(EhCacheConfigurationFactory.EHCACHE_LISTENER_HOSTNAME)).thenReturn("special-host");

        final EhCacheConfigurationFactory configurationFactory = new EhCacheConfigurationFactory();
        Configuration config = configurationFactory.newConfiguration(configUrl, clusterNodeProperties);

        String peerListenerProperties = config.getCacheManagerPeerListenerFactoryConfigurations().get(0).getProperties();
        Properties listenerProps = PropertyUtil.parseProperties(peerListenerProperties, null);
        assertThat(listenerProps.getProperty("hostName"), is("special-host"));
    }

}
