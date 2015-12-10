package com.atlassian.jira.cluster.distribution;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;

import static com.atlassian.jira.cluster.distribution.JiraRMICacheManagerPeerProviderFactory.PEER_DISCOVERY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 6.3.4
 */
@RunWith (MockitoJUnitRunner.class)
public class TestJiraRmiCacheManagerPeerProviderFactory
{

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Properties properties;


    @Test
    public void testDefaultPeerProvider()
    {
        JiraRMICacheManagerPeerProviderFactory factory = new JiraRMICacheManagerPeerProviderFactory();
        CacheManagerPeerProvider peerProvider = factory.createCachePeerProvider(cacheManager, properties);
        assertThat(peerProvider, instanceOf(JiraCacheManagerPeerProvider.class));
    }

    @Test
    public void testAutomaticPeerProvider() throws Exception
    {
        MyPeerProviderFactory factory = new MyPeerProviderFactory();

        when(properties.get(PEER_DISCOVERY)).thenReturn("automatic");
        when(properties.size()).thenReturn(1);

        factory.createCachePeerProvider(cacheManager, properties);
        assertThat(factory.autoCalled, is(true));
        assertThat(factory.manualCalled, is(false));
    }

    @Test
    public void testManualPeerProvider() throws Exception
    {
        MyPeerProviderFactory factory = new MyPeerProviderFactory();

        when(properties.get(PEER_DISCOVERY)).thenReturn("manual");
        when(properties.size()).thenReturn(1);

        factory.createCachePeerProvider(cacheManager, properties);
        assertThat(factory.autoCalled, is(false));
        assertThat(factory.manualCalled, is(true));
    }

}

class MyPeerProviderFactory extends JiraRMICacheManagerPeerProviderFactory
{

    boolean manualCalled = false;
    boolean autoCalled = false;

    @Override
    protected CacheManagerPeerProvider createManuallyConfiguredCachePeerProvider(final Properties properties)
    {
        manualCalled = true;
        return mock(CacheManagerPeerProvider.class);
    }

    @Override
    protected CacheManagerPeerProvider createAutomaticallyConfiguredCachePeerProvider(final CacheManager cacheManager, final Properties properties)
            throws IOException
    {
        autoCalled = true;
        return mock(CacheManagerPeerProvider.class);
    }
}
