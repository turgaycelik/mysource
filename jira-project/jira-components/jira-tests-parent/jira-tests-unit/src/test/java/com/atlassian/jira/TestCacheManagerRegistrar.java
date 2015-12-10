package com.atlassian.jira;

import java.io.File;
import java.net.URL;

import javax.management.MBeanServer;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.ehcache.EhCacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.cache.CacheCompactor;
import com.atlassian.jira.cache.EhCacheCompactor;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.cluster.EhCacheConfigurationFactory;
import com.atlassian.jira.config.properties.JiraSystemProperties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.ehcache.distribution.MulticastKeepaliveHeartbeatSender;

import static com.atlassian.jira.CacheManagerRegistrar.COMPACTOR_KEY;
import static com.atlassian.jira.CacheManagerRegistrar.COMPACTOR_SCOPE;
import static com.atlassian.jira.CacheManagerRegistrar.EHCACHE_CONFIGURATION;
import static com.atlassian.jira.CacheManagerRegistrar.ENABLE_JMX;
import static com.atlassian.jira.CacheManagerRegistrar.FORCE_EHCACHE;
import static com.atlassian.jira.CacheManagerRegistrar.MANAGER_KEY;
import static com.atlassian.jira.CacheManagerRegistrar.MANAGER_SCOPE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCacheManagerRegistrar
{
    private static final String CUSTOM_EHCACHE_CONFIG = "/test-ehcache.xml";
    private long oldHeartBeatInterval;

    /**
     * Sets the system property that enforces the use of Ehcache.
     */
    private static void forceEhcache()
    {
        setSystemProperty(FORCE_EHCACHE, true);
    }
    
    private static Cache<Integer, String> createCacheWithTwoPuts(final CacheManager cacheManager)
    {
        final Cache<Integer, String> cache = cacheManager.getCache("myCache");
        cache.put(1, "foo");
        cache.put(2, "bar");
        return cache;
    }

    private static void setSystemProperty(final String property, final boolean value)
    {
        setSystemProperty(property, String.valueOf(value));
    }

    private static void setSystemProperty(final String property, final String value)
    {
        JiraSystemProperties.getInstance().setProperty(property, value);
    }

    @Mock private ComponentContainer mockComponentContainer;
    @Mock private ClusterNodeProperties mockClusterNodeProperties;
    @Mock private MBeanServer mockMBeanServer;

    @Before
    public void setUp()
    {
        // Turn down the heartbeat interval to speed up these tests.
        oldHeartBeatInterval = MulticastKeepaliveHeartbeatSender.getHeartBeatInterval();
        MulticastKeepaliveHeartbeatSender.setHeartBeatInterval(100);

        setSystemProperty(EHCACHE_CONFIGURATION, null);
        setSystemProperty(ENABLE_JMX, false);
        setSystemProperty(FORCE_EHCACHE, false);
        when(mockComponentContainer.getComponentInstance(ClusterNodeProperties.class))
                .thenReturn(mockClusterNodeProperties);
    }

    @After
    public void tearDown()
    {
        setSystemProperty(EHCACHE_CONFIGURATION, null);
        setSystemProperty(ENABLE_JMX, false);
        setSystemProperty(FORCE_EHCACHE, false);

        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getCacheManager(EhCacheConfigurationFactory.MANAGER_NAME);
        if (ehCacheManager != null)
        {
            ehCacheManager.shutdown();
        }
        MulticastKeepaliveHeartbeatSender.setHeartBeatInterval(oldHeartBeatInterval);
    }

    @Test
    public void shouldRegisterEhCacheManagerWhenEhcacheIsForced()
    {
        forceEhcache();
        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);

        assertCacheManager(EhCacheManager.class);
        assertCacheCompactor(EhCacheCompactor.class);
    }

    @Test
    public void shouldRegisterMemoryCacheManagerByDefault()
    {
        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);
        assertCacheManager(MemoryCacheManager.class);
    }

    @Test
    public void shouldRegisterEhCacheManagerWhenJiraIsClustered()
    {
        setUpClustering();
        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);
        assertCacheManager(EhCacheManager.class);
    }

    @Test
    public void shouldRegisterEhCacheManagerWhenJiraIsClusteredAndEhcacheIsForced()
    {
        setUpClustering();
        forceEhcache();
        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);
        assertCacheManager(EhCacheManager.class);
    }

    private void setUpClustering()
    {
        when(mockClusterNodeProperties.getNodeId()).thenReturn("anything");
    }

    private CacheManager assertCacheManager(final Class<? extends CacheManager> expectedCacheManagerClass)
    {
        final ArgumentCaptor<CacheManager> cacheManagerCaptor = ArgumentCaptor.forClass(CacheManager.class);
        verify(mockComponentContainer).instance(eq(MANAGER_SCOPE), eq(MANAGER_KEY), cacheManagerCaptor.capture());
        final CacheManager cacheManager = cacheManagerCaptor.getValue();
        assertNotNull(cacheManager);
        assertEquals(expectedCacheManagerClass, cacheManager.getClass());
        return cacheManager;
    }

    private void assertCacheCompactor(final Class<EhCacheCompactor> ehCacheCompactorClass)
    {
        final ArgumentCaptor<CacheCompactor> cacheCompactorCaptor = ArgumentCaptor.forClass(CacheCompactor.class);
        verify(mockComponentContainer).instance(eq(COMPACTOR_SCOPE), eq(COMPACTOR_KEY), cacheCompactorCaptor.capture());
        final CacheCompactor cacheManager = cacheCompactorCaptor.getValue();
        assertNotNull(cacheManager);
        assertEquals(ehCacheCompactorClass, cacheManager.getClass());
        return;
    }

    @Test
    public void shouldDefaultToBuiltInEhcacheConfigurationIfCustomEhcacheConfigurationUrlIsInvalid()
    {
        // Set up
        forceEhcache();
        setSystemProperty(EHCACHE_CONFIGURATION, "fubar");

        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);

        final CacheManager cacheManager = assertCacheManager(EhCacheManager.class);

        // Check
        final Cache<Integer, String> cache = createCacheWithTwoPuts(cacheManager);
        assertEquals(2, cache.getKeys().size());
    }

    @Test
    public void shouldUseCustomEhcacheConfigurationIfCustomEhcacheConfigurationUrlIsValid() throws Exception
    {
        // Set up
        forceEhcache();
        final URL customConfigUrl = getClass().getResource(CUSTOM_EHCACHE_CONFIG);
        assertNotNull("Can't find in classpath: " + CUSTOM_EHCACHE_CONFIG, customConfigUrl);
        setSystemProperty(EHCACHE_CONFIGURATION, new File(customConfigUrl.toURI()).getAbsolutePath());

        // Invoke
        CacheManagerRegistrar.registerCacheManager(mockComponentContainer, mockMBeanServer);

        final CacheManager cacheManager = assertCacheManager(EhCacheManager.class);

        // Check
        final Cache<Integer, String> cache = createCacheWithTwoPuts(cacheManager);
        // If we've truly picked up the custom config, max entries should be 1
        assertEquals(1, cache.getKeys().size());
    }
}
