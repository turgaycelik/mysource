package com.atlassian.jira.bc.dataimport.ha;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.dataimport.ImportCompletedEvent;
import com.atlassian.jira.bc.dataimport.ImportStartedEvent;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.EventMessageConsumer;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

/**
 * Listens for imports and takes the appropriate actions
 *
 * @since v6.1
 */
@EventComponent
public class ClusterImportListener
{

    private final MessageHandlerService messageHandlerService;
    private final ClusterImportService clusterImportService;

    private final Event importEvent = new Event(EventType.get("import"), "JIRA is currently being restored from backup",
            EventLevel.get(EventLevel.WARNING));

    public ClusterImportListener(final MessageHandlerService messageHandlerService, final ClusterImportService clusterImportService)
    {
        this.messageHandlerService = messageHandlerService;
        this.clusterImportService = clusterImportService;
    }

    @EventListener
    public void onImportStarted(ImportStartedEvent importStartedEvent)
    {
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, EventMessageConsumer.importStartedMessage());
    }

    @EventListener
    public void onImportCompleted(ImportCompletedEvent importCompletedEvent)
    {
        clusterImportService.prepareImport();
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, EventMessageConsumer.importFinishedMessage(importCompletedEvent.isImportSuccessful()));
    }

    @EventListener
    public void onRemoteImportCompleted(RemoteImportCompletedEvent remoteImportCompletedEvent)
    {
        if (remoteImportCompletedEvent.isImportSuccessful())
        {
            clusterImportService.doImport(remoteImportCompletedEvent.getIndexBackupFileName());
        }
    }
}
