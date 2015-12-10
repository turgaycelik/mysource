package com.atlassian.jira.cluster.distribution;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.mock.component.MockComponentWorker;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CachePeer;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestJiraCacheManagerPeerProvider
{

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ClusterManager clusterManager;

    private JiraCacheManagerPeerProvider provider;

    @Mock
    private Ehcache ehcache;

    @Before
    public void setUp() throws Exception
    {
        provider = new MyJiraProvider(cacheManager);
        final MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(ClusterManager.class, clusterManager).init();
    }

    @Test
    public void listAllPeersExceptMe() throws RemoteException
    {
        Node node1 = new Node("node1", Node.NodeState.ACTIVE, Long.MAX_VALUE, "localhost", 43l);
        Node node2 = new Node("node2", Node.NodeState.ACTIVE, Long.MAX_VALUE, "127.0.0.1", 44l);

        when(clusterManager.getNodeId()).thenReturn("node1");
        when(clusterManager.findLiveNodes()).thenReturn(ImmutableSet.of(node1, node2));
        when(ehcache.getName()).thenReturn("cacheName");

        final List<CachePeer> list = provider.listRemoteCachePeers(ehcache);
        assertThat(list, hasSize(1));
        assertThat(list.get(0).getUrl(), is("//127.0.0.1:44/cacheName"));
    }

    @Test
    public void notListWhileContainerIsNotReady()
    {
        Node node1 = new Node("node1", Node.NodeState.ACTIVE, Long.MAX_VALUE, "localhost", 43l);
        Node node2 = new Node("node2", Node.NodeState.ACTIVE, Long.MAX_VALUE, "127.0.0.1", 44l);

        when(clusterManager.getNodeId()).thenReturn("node1");
        when(clusterManager.findLiveNodes()).thenReturn(ImmutableSet.of(node1, node2));
        when(ehcache.getName()).thenReturn("cacheName");

        // We use the proper manager and it will always return an empty list
        provider = new JiraCacheManagerPeerProvider(cacheManager);
        assertThat(provider.listRemoteCachePeers(ehcache), hasSize(0));
    }
}

class MyJiraProvider extends JiraCacheManagerPeerProvider
{
    public MyJiraProvider(final CacheManager cacheManager)
    {
        super(cacheManager);
    }

    /**
     * Override this method so we don't deal with the container
     *
     * @return true always
     */
    @Override
    protected boolean isContainerInitialized()
    {
        return true;
    }

    /**
     * Override this method so the lookup does not fail.
     */
    @Override
    public CachePeer lookupRemoteCachePeer(final String url)
            throws MalformedURLException, NotBoundException, RemoteException
    {
        final CachePeer peer = mock(CachePeer.class);
        when(peer.getUrl()).thenReturn(url);
        return peer;
    }
}
