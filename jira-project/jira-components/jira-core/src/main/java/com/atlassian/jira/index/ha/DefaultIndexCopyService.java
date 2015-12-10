package com.atlassian.jira.index.ha;

import java.io.File;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.PathUtils;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;

/**
 * Backs up the index to the shared home folder
 *
 * @since v6.1
 */
public class DefaultIndexCopyService implements IndexCopyService
{
    public final static String BACKUP_INDEX_DONE = "Index Backed Up";
    public final static String BACKUP_INDEX = "Backup Index";

    private static final Logger LOG = Logger.getLogger(DefaultIndexCopyService.class);
    private static final String INDEX_BACKUP_SEQUENCE = "IndexBackupSequence";

    private MessageConsumer messageConsumer;


    @SuppressWarnings("UnusedDeclaration")  // Used by Pico
    public DefaultIndexCopyService(final IndexPathManager indexPathManager, final JiraHome jiraHome,
            final IndexUtils indexUtils, final MessageHandlerService messageHandlerService,
            final EventPublisher eventPublisher, final IndexRecoveryManager indexRecoveryManager,
            final DelegatorInterface delegatorInterface, final I18nHelper i18n,
            final OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore)
    {
        final String sharedIndexPath = PathUtils.joinPaths(jiraHome.getHome().getAbsolutePath(), JiraHome.CACHES);
        messageConsumer = new MessageConsumer(indexUtils, indexRecoveryManager, delegatorInterface,
                indexPathManager.getIndexRootPath(), messageHandlerService, sharedIndexPath, eventPublisher,
                ofBizNodeIndexOperationStore);
        messageHandlerService.registerListener(BACKUP_INDEX, messageConsumer);
        messageHandlerService.registerListener(BACKUP_INDEX_DONE, messageConsumer);
    }

    /**
     *  Backs up all the index files from local home to shared home. Any index in shared home will be cleared.
     */
    @Override
    public String backupIndex(final String requestingNode)
    {
        return messageConsumer.backupIndex(requestingNode);
    }

    /**
     * copy all the index files form shared home into local home.
     */
    @Override
    public void restoreIndex(final String filePath)
    {
        messageConsumer.restoreIndex(filePath);
    }

    /**
     *
     * @param sourcePath  the path of the index to copy
     * @param destinationPath  where to copy the index to
     * @param id Index backup id, Should be unique across the JIRA instance
     * @return the name of the generated backup file.
     */
    @VisibleForTesting
    String copyIndex(final String sourcePath, final String destinationPath, final Long id)
    {
        return messageConsumer.copyIndex(sourcePath, destinationPath, id);
    }


    private static class MessageConsumer implements ClusterMessageConsumer
    {
        private static final int MAX_SNAPSHOTS = 3;

        private final IndexUtils indexUtils;
        private final IndexRecoveryManager indexRecoveryManager;
        private final DelegatorInterface delegatorInterface;
        private final String localIndexPath;
        private final MessageHandlerService messageHandlerService;
        private final String sharedIndexPath;
        private final EventPublisher eventPublisher;
        private final OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore;
        private final ComponentReference<ClusterManager> clusterManagerRef = ComponentAccessor.getComponentReference(ClusterManager.class);

        public MessageConsumer(final IndexUtils indexUtils, final IndexRecoveryManager indexRecoveryManager, final DelegatorInterface delegatorInterface,
                final String localIndexPath, final MessageHandlerService messageHandlerService, final String sharedIndexPath,
                final EventPublisher eventPublisher, final OfBizReplicatedIndexOperationStore ofBizNodeIndexOperationStore)
        {
            this.indexUtils = indexUtils;
            this.indexRecoveryManager = indexRecoveryManager;
            this.delegatorInterface = delegatorInterface;
            this.localIndexPath = localIndexPath;
            this.messageHandlerService = messageHandlerService;
            this.sharedIndexPath = sharedIndexPath;
            this.eventPublisher = eventPublisher;
            this.ofBizNodeIndexOperationStore = ofBizNodeIndexOperationStore;
        }

        public String backupIndex(String requestingNode)
        {
            if (clusterManagerRef.get().isClustered())
            {
                // nodes not having index should not respond
                final String nodeId = clusterManagerRef.get().getNodeId();
                final Long latestOperation = ofBizNodeIndexOperationStore.getLatestOperation(nodeId);
                if (latestOperation != null)
                {
                    Long backupId = delegatorInterface.getNextSeqId(INDEX_BACKUP_SEQUENCE);
                    String backupFileName = copyIndex(localIndexPath, sharedIndexPath, backupId);
                    if (!requestingNode.equals(nodeId))
                    {
                        messageHandlerService.sendMessage(requestingNode, new Message(BACKUP_INDEX_DONE, backupFileName));
                    }
                    return backupFileName;
                }
            }
            return null;
        }

        /**
         *
         * @param sourcePath  the path of the index to copy
         * @param destinationPath  where to copy the index to
         * @param id Index backup id, Should be unique across the JIRA instance
         * @return the name of the generated backup file.
         */
        @VisibleForTesting
        String copyIndex(final String sourcePath, final String destinationPath, final Long id)
        {
            return indexUtils.takeIndexSnapshot(sourcePath, destinationPath, id.toString(), MAX_SNAPSHOTS);
        }

        public void restoreIndex(String fileName)
        {
            if (clusterManagerRef.get().isClustered())
            {
                LOG.info("Index restore started");
                final File backupFile = new File(sharedIndexPath, fileName);
                try
                {
                    indexRecoveryManager.recoverIndexFromBackup(backupFile, TaskProgressSink.NULL_SINK);
                }
                catch (IndexException e)
                {
                    throw new RuntimeException(e);
                }
                eventPublisher.publish(IndexesRestoredEvent.INSTANCE);
                LOG.info("Index restore complete");
            }
        }

        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (channel.equals(BACKUP_INDEX))
            {
                backupIndex(senderId);
            }
            else if (channel.equals(BACKUP_INDEX_DONE))
            {
                restoreIndex(message);
            }
        }

    }
}
