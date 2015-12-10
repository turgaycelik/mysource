package com.atlassian.jira.bc.dataimport.ha;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.NodeStateManager;
import com.atlassian.jira.cluster.NotClusteredException;
import com.atlassian.jira.index.ha.IndexCopyService;

/**
 *
 * @since v6.1
 */
public class DefaultClusterImportService implements ClusterImportService
{
    private final NodeStateManager nodeStateManager;
    private final IndexCopyService indexCopyService;


    public DefaultClusterImportService(final NodeStateManager nodeStateManager, final IndexCopyService indexCopyService)
    {
        this.nodeStateManager = nodeStateManager;
        this.indexCopyService = indexCopyService;
    }

    @Override
    public void prepareImport()
    {
        indexCopyService.backupIndex(ClusterManager.ANY_NODE);
    }

    @Override
    public void doImport(String fileName)
    {
        try
        {
            nodeStateManager.quiesce();
            indexCopyService.restoreIndex(fileName);
            nodeStateManager.restart();
        }
        catch (NotClusteredException e)
        {
            throw new RuntimeException(e);
        }

    }
}
