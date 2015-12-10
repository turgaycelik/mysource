package com.atlassian.jira.cluster;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.cluster.lock.SharedHomeNodeStatusWriter;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.cluster.ClusterMessageCleaningService;
import com.atlassian.jira.service.services.cluster.NodeStateCheckerService;
import com.atlassian.jira.service.services.index.ReplicatedIndexCleaningService;
import com.atlassian.jira.util.I18nHelper;

import org.apache.log4j.Logger;

/**
 * Starts and stops clustering specific services - checks to see if clustered
 *
 * @since v6.1
 */
public class DefaultClusterServicesManager implements ClusterServicesManager, Startable
{
    private static final int MINS_PER_DAY = 24 * 60;

    private final MessageHandlerService messageHandlerService;
    private final ClusterManager clusterManager;
    private final NodeReindexService nodeReindexService;
    private final ServiceManager serviceManager;
    private final I18nHelper i18nHelper;
    private final NodeStateManager nodeStateManager;
    private final ClusterNodeHeartBeatDao heartBeatDao;
    private final SharedHomeNodeStatusWriter sharedHomeNodeStatusWriter;

    private static final Logger LOG = Logger.getLogger(ClusterServicesManager.class);

    public DefaultClusterServicesManager(final ClusterManager clusterManager, final ServiceManager serviceManager,
            final ClusterServicesRegistry clusterServicesRegistry, final I18nHelper i18nHelper,
            final NodeStateManager nodeStateManager, final ClusterNodeHeartBeatDao heartBeatDao,
            final SharedHomeNodeStatusWriter sharedHomeNodeStatusWriter)
    {
        this.clusterManager = clusterManager;
        this.serviceManager = serviceManager;
        this.i18nHelper = i18nHelper;
        this.messageHandlerService = clusterServicesRegistry.getMessageHandlerService();
        this.nodeReindexService = clusterServicesRegistry.getNodeReindexService();
        this.nodeStateManager = nodeStateManager;
        this.heartBeatDao = heartBeatDao;
        this.sharedHomeNodeStatusWriter = sharedHomeNodeStatusWriter;
    }

    @Override
    public void startServices()
    {
        if (clusterManager.isClustered())
        {
            nodeReindexService.start();
            messageHandlerService.start();
            new IndexCleaningServiceHelper().createIndexCleaningServiceIfNeeded(i18nHelper);
            new ClusterMessageCleaningServiceHelper().createClusterMessageCleaningServiceIfNeeded(i18nHelper);
            new NodeCheckerServiceHelper().createNodeCheckerServiceIfNeeded(i18nHelper);
        }
    }

    @Override
    public void stopServices()
    {
        nodeReindexService.cancel();
        messageHandlerService.stop();

        //We set the node in OFFLINE mode, means that a user shutdown the server.
        nodeStateManager.shutdownNode();

        // We add the heartbeat in zero to keep consistency. The node exist in both tables
        // but it is disabled
        heartBeatDao.writeHeartBeat(0);

        //Remove the status file from shared home for this node
        sharedHomeNodeStatusWriter.removeNodeStatus(heartBeatDao.getNodeId());
    }

    @Override
    public void start() throws Exception
    {
        this.startServices();
    }



    private final class IndexCleaningServiceHelper
    {
        private static final String SERVICE_NAME_KEY = "admin.services.indexcleaner.service";
        private static final String DEFAULT_RETENTION_MINS = "" + (2 * MINS_PER_DAY) + 'm';
        // by default every 12 hours
        private final long DELAY = DateUtils.HOUR_MILLIS * 12;

        /**
         * Creates a new IndexCleaning service if it doesn't exist.
         */
        public void createIndexCleaningServiceIfNeeded(final I18nHelper i18n)
        {
            try
            {
                final String serviceName = i18n.getText(SERVICE_NAME_KEY);
                if (serviceManager.getServiceWithName(serviceName) == null)
                {
                    final Map<String, String[]> params = new HashMap<String, String[]>(2);
                    params.put(ReplicatedIndexCleaningService.RETENTION_PERIOD, new String[] { DEFAULT_RETENTION_MINS });
                    serviceManager.addService(serviceName, ReplicatedIndexCleaningService.class.getName(), DELAY, params);
                }
            }
            catch (final Exception e) // intentionally catching RuntimeException as well
            {
                LOG.error(i18nHelper.getText("admin.errors.setup.error.adding.service", e.toString()));
            }
        }

    }
    private final class ClusterMessageCleaningServiceHelper
    {
        private static final String SERVICE_NAME_KEY = "admin.services.cluster.message.cleaner";
        private static final String DEFAULT_RETENTION_MINS = "" + (2 * MINS_PER_DAY) + 'm';
        // by default every 12 hours
        private final long DELAY = DateUtils.HOUR_MILLIS * 12;

        /**
         * Creates a new ClusterMessageCleaningCleaning service if it doesn't exist.
         */
        public void createClusterMessageCleaningServiceIfNeeded(final I18nHelper i18n)
        {
            try
            {
                final String serviceName = i18n.getText(SERVICE_NAME_KEY);
                if (serviceManager.getServiceWithName(serviceName) == null)
                {
                    final Map<String, String[]> params = new HashMap<String, String[]>(2);
                    params.put(ClusterMessageCleaningService.RETENTION_PERIOD, new String[] { DEFAULT_RETENTION_MINS });
                    serviceManager.addService(serviceName, ClusterMessageCleaningService.class.getName(), DELAY, params);
                }
            }
            catch (final Exception e) // intentionally catching RuntimeException as well
            {
                LOG.error(i18nHelper.getText("admin.errors.setup.error.adding.service", e.toString()));
            }
        }

    }
    private final class NodeCheckerServiceHelper
    {
        private static final String SERVICE_NAME_KEY = "admin.services.nodechecker.service";
        // by default every minute
        private final long DELAY = DateUtils.MINUTE_MILLIS * 1;

        /**
         * Creates a new Node State Checker service if it doesn't exist.
         */
        public void createNodeCheckerServiceIfNeeded(final I18nHelper i18n)
        {
            try
            {
                final String serviceName = i18n.getText(SERVICE_NAME_KEY);
                if (serviceManager.getServiceWithName(serviceName) == null)
                {
                    final Map<String, String[]> params = new HashMap<String, String[]>(0);
                    serviceManager.addService(serviceName, NodeStateCheckerService.class.getName(), DELAY, params);
                }
            }
            catch (final Exception e) // intentionally catching RuntimeException as well
            {
                LOG.error(i18nHelper.getText("admin.errors.setup.error.adding.service", e.toString()));
            }
        }
    }
}
