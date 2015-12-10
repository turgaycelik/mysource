package com.atlassian.jira.cluster.distribution;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.util.log.RateLimitingLogger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.distribution.RMICacheManagerPeerProvider;

/**
 * Jira Cache Manager Peer Provider This provider will match with the cluster manager to get all the live nodes and
 * build the proper rmi points where to propagate the information
 *
 * @since 6.3.4
 */
public class JiraCacheManagerPeerProvider extends RMICacheManagerPeerProvider
{
    private static final RateLimitingLogger log = new RateLimitingLogger(JiraCacheManagerPeerProvider.class);

    public JiraCacheManagerPeerProvider(CacheManager cacheManager)
    {
        super(cacheManager);
    }

    private final ComponentReference<ClusterManager> clusterManagerRef = ComponentAccessor.getComponentReference(ClusterManager.class);

    @Override
    public void init()
    {
    }

    @Override
    public long getTimeForClusterToForm()
    {
        return 0;
    }

    @Override
    public final void registerPeer(String rmiUrl)
    {
        //Nothing to do here
    }

    @Override
    public List<CachePeer> listRemoteCachePeers(Ehcache cache) throws CacheException
    {
        final List<CachePeer> remoteCachePeers = new ArrayList<CachePeer>();

        if (!isContainerInitialized())
        {
            return Collections.emptyList();
        }

        final ClusterManager clusterManager = getClusterManager();
        final String currentNodeId = clusterManager.getNodeId();
        final Collection<Node> liveNodes = clusterManager.findLiveNodes();

        final Collection<Node> nodesToPropagate = Collections2.filter(liveNodes, new Predicate<Node>()
        {
            @Override
            public boolean apply(@Nullable Node node)
            {
                return node != null && !currentNodeId.equals(node.getNodeId());
            }
        });

        for (Node node : nodesToPropagate)
        {
            final String rmiUrl = buildBaseUrl(node, cache.getName());

            try
            {
                remoteCachePeers.add(lookupRemoteCachePeer(rmiUrl));
            }
            catch (RemoteException e)
            {
                log.warn("Looking up rmiUrl " + rmiUrl + " threw a connection exception. This could mean that a node has gone offline "
                        + " or it may indicate network connectivity difficulties. Details: " + e.getMessage());
            }
            catch (MalformedURLException e)
            {
                log.error("Looking up rmiUrl " + rmiUrl + " through exception . Urls are not well formed. Please fix this.");
            }
            catch (NotBoundException e)
            {
                log.debug("Looking up rmiUrl " + rmiUrl + " threw a connection exception. This may be normal if a node has gone offline."
                        + " Or it may indicate network connectivity difficulties. Details : " + e.getMessage());
            }
        }

        return remoteCachePeers;
    }

    @Override
    protected boolean stale(Date date)
    {
        return false;
    }

    private String buildBaseUrl(final Node node, final String cacheName)
    {
        return "//" + node.getIp() + ':' + node.getCacheListenerPort() + '/' + cacheName;
    }

    /**
     * We need to do this cause we cannot assign the cluster manager in initialization
     *
     * @return the cluster manager
     */
    private ClusterManager getClusterManager()
    {
        return clusterManagerRef.get();
    }

    protected boolean isContainerInitialized()
    {
        return ComponentManager.getInstance().getState().isContainerInitialised();
    }
}
