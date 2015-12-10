package com.atlassian.jira.index.ha;

import com.atlassian.annotations.Internal;

import com.google.common.annotations.VisibleForTesting;

/**
 *
 * @since v6.1
 */
public interface IndexCopyService
{
    /**
     * Back up an index for replicating to another node
     * @param requestingNode Node requesting the index
     * @return the name of the backup file, or null if the backup did not occur
     */
    String backupIndex(String requestingNode);

    /**
     * Restore an index.
     * @param filePath path of the zip file containing the index backup.
     */
    void restoreIndex(String filePath);
}
