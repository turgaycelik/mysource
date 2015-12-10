package com.atlassian.jira.index.ha;

import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

import java.io.File;

/**
 * Manager to recover an index from a previous index backup
 *
 * @since v6.2
 */
public interface IndexRecoveryManager extends Sized
{
    /**
     * Recovers an index from an index backup.
     *
     * @param recoveryFile The backup file
     * @param taskProgressSink
     * @throws IndexException If we are unable to recover the index
     */
    IndexCommandResult recoverIndexFromBackup(File recoveryFile, TaskProgressSink taskProgressSink) throws IndexException;

}
