package com.atlassian.jira.cluster.distribution;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import net.sf.ehcache.util.PropertyUtil;

/**
 * Factory that will maintain the functionality of ehcache peer provider but will add the functionality of jira also if
 * it wants to be activated
 *
 * @since 6.3.4
 */
public class JiraRMICacheManagerPeerProviderFactory extends RMICacheManagerPeerProviderFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(JiraRMICacheManagerPeerProviderFactory.class);
    public static final String PEER_DISCOVERY = "peerDiscovery";


    @Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties properties)
            throws CacheException
    {
        final String peerDiscovery = PropertyUtil.extractAndLogProperty(PEER_DISCOVERY, properties);

        LOG.info(" Starting Jira instance with {} cache replication strategy", (peerDiscovery == null ? "default" : peerDiscovery));

        if (peerDiscovery == null || StringUtils.equalsIgnoreCase(peerDiscovery, "default"))
        {
            return new JiraCacheManagerPeerProvider(cacheManager);
        }

        return super.createCachePeerProvider(cacheManager, properties);
    }

}
