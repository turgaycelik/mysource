package com.atlassian.jira.upgrade;

import com.atlassian.jira.bean.export.IllegalXMLCharactersException;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface UpgradeManager
{

    /**
     * <p/>
     * Performs any upgrades that may be needed as a result of the Setup procedure of JIRA
     *
     * <p/>
     * Get the set of setupUpgradeNumbers which are to be performed for this setup.
     *
     * <p/>
     * Iterate over these numbers and if either of the standard, professional or enterprise upgrade maps contains an
     * {@link UpgradeTask} with this number then do the upgrade
     *
     * <p/>
     * If errors are found, it will cancel the upgrade, and return the list of errors.
     *
     * <p/>
     * For each upgrade that happens successfully, it will increment the build number in the database, so that if one
     * fails, you do not have to repeat all the upgrades that have already run.
     *
     * <p/>
     * If there are no errors from the upgrade, the build number in the database is incremented to the current build
     * number.  This is because there may be no upgrades for a particular version & needUpgrade() checks build no in
     * database.
     *
     * @return status of the upgrade process
     */
    Status doSetupUpgrade();

    /**
     * Performs the upgrade if one is required and the license is not too old to proceed with the upgrade.
     *
     * @param backupPath - a path to the default location of the export, may be <code>null</code>, in which case no auto
     * export will be performed
     * @param setupMode
     * @return status of the upgrade process
     * @throws com.atlassian.jira.bean.export.IllegalXMLCharactersException if backup was impossible due to invalid XML
     * characters
     */
    Status doUpgradeIfNeededAndAllowed(@Nullable String backupPath, boolean setupMode) throws IllegalXMLCharactersException;

    /**
     * Export path of the last backup performed by this manager
     *
     * @return path to the last backup file
     */
    String getExportFilePath();

    /**
     * @return the history of upgrades performed on this instance of JIRA in reverse chronological order
     * @since v4.1
     */
    List<UpgradeHistoryItem> getUpgradeHistory();

    /**
     * Status of the upgrade process
     */
    public static class Status
    {
        private final boolean reindexPerformed;
        private final Collection<String> errors;

        /**
         * Creates status with only one error message and reindexPerformed flag set to false
         * @param error the only erro messag
         */
        public Status(String error)
        {
            reindexPerformed = false;
            errors = ImmutableList.of(error);
        }

        /**
         *
         * @param reindexPerformed flag indicating if reindexing was performed during upgrade
         * @param errors a list of errors that occurred during the upgrade
         */
        public Status(boolean reindexPerformed, Collection<String> errors)
        {
            this.reindexPerformed = reindexPerformed;
            this.errors = errors;
        }

        /**
         * If one of the executed upgrade tasks required reindexing thim method will return true
         * @return true if upgrade manager performed deindexing
         */
        public boolean reindexPerformed()
        {
            return reindexPerformed;
        }

        /**
         * Checks wether this upgrade was succesful
         * @return true if upgrade tasks returned no erros
         */
        public boolean succesful()
        {
            return CollectionUtils.isEmpty(errors);
        }

        /**
         * List of errors that occurred during the upgrade
         * @return list of errors
         */
        public Collection<String> getErrors()
        {
            return errors;
        }
    }
}
