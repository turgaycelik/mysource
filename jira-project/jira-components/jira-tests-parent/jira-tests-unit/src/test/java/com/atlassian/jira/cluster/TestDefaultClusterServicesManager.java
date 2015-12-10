package com.atlassian.jira.cluster;

import com.atlassian.beehive.db.spi.ClusterNodeHeartBeatDao;
import com.atlassian.jira.cluster.lock.SharedHomeNodeStatusWriter;
import com.atlassian.jira.index.ha.NodeReindexService;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultClusterServicesManager
{
    @Mock
    private NodeReindexService mockReindexService;
    @Mock
    private MessageHandlerService mockMessageHandlerService;
    @Mock
    private ServiceManager mockServiceManager;
    @Mock
    private ClusterServicesRegistry mockClusterServicesRegistry;
    @Mock
    private I18nHelper mockI18nHelper;
    @Mock
    private ClusterManager mockClusterManager;
    @Mock
    private NodeStateManager nodeStateManager;
    @Mock
    private ClusterNodeHeartBeatDao heartBeatDao;
    @Mock
    private SharedHomeNodeStatusWriter sharedHomeNodeStatusWriter;

    private ClusterServicesManager clusterServicesManager;

    @Before
    public void setupMocks() throws Exception
    {
        when(mockClusterManager.isClustered()).thenReturn(false);
        when(mockClusterServicesRegistry.getNodeReindexService()).thenReturn(mockReindexService);
        when(mockClusterServicesRegistry.getMessageHandlerService()).thenReturn(mockMessageHandlerService);
        clusterServicesManager = new DefaultClusterServicesManager(mockClusterManager, mockServiceManager, mockClusterServicesRegistry, mockI18nHelper, nodeStateManager, heartBeatDao, sharedHomeNodeStatusWriter);
    }

    @Test
    public void testStartReindexService()
    {
        makeClustered();
        clusterServicesManager.startServices();
        verify(mockReindexService).start();
    }

    private void makeClustered()
    {
        when(mockClusterManager.isClustered()).thenReturn(true);
    }

    @Test
    public void testStartMessageService()
    {
        makeClustered();
        clusterServicesManager.startServices();
        verify(mockMessageHandlerService).start();
    }

    @Test
    public void testCancelIndexService()
    {
        clusterServicesManager.stopServices();
        verify(mockReindexService).cancel();
        verify(nodeStateManager).shutdownNode();
        verify(heartBeatDao).writeHeartBeat(0);
    }

    @Test
    public void testThatStopCleansSharedHomeClusterInfoFile()
    {
        makeClustered();
        when(heartBeatDao.getNodeId()).thenReturn("node1");

        clusterServicesManager.stopServices();
        verify(sharedHomeNodeStatusWriter).removeNodeStatus("node1");
    }
}
