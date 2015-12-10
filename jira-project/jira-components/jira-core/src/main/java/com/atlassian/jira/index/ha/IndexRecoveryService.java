package com.atlassian.jira.index.ha;

import java.io.File;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.jira.web.action.admin.index.EditIndexRecoverySettings;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

/**
 * Manager to recover an index from a previous index backup
 *
 * @since v6.2
 */
public interface IndexRecoveryService extends Sized
{
    /**
     * Recovers an index from an index backup
     * @param user the logged in user
     * @param context Context
     * @param i18n
     * @param recoveryFilename The backup file
     * @param taskProgressSink
     * @throws com.atlassian.jira.issue.index.IndexException If we are unable to recover the index
     */
    IndexCommandResult recoverIndexFromBackup(ApplicationUser user, Context context, I18nHelper i18n, String recoveryFilename, TaskProgressSink taskProgressSink)
            throws IndexException;

    /**
     * Validate the passed in file to be a valid zip file containing a set of Lucene Index directories.
     * @param user the logged in user
     * @param zipFile File to validate
     * @return true if all OK
     */
    boolean validIndexZipFile(final ApplicationUser user, File zipFile);

    /**
     * Is index recovery enabled.
     * @return true if it is.
     */
    boolean isRecoveryEnabled(final ApplicationUser user);

    /**
     * Get the interval configured for taking recovery snapshots
     * @param user the logged in user
     * @return interval in Millis or null if this is undefined
     */
    @Nullable
    Long getSnapshotInterval(final ApplicationUser user);

    /**
     * Update the recovery settings
     * @param user the logged in user
     * @param recoveryEnabled is recovery enabled
     * @param snapshotInterval Interval at which ti take snapshots
     */
    void updateRecoverySettings(final ApplicationUser user, boolean recoveryEnabled, long snapshotInterval)
            throws Exception;
}
