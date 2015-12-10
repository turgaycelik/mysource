package com.atlassian.jira.issue.changehistory.metadata;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows storing and retrieving metadata related to issue history entries.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
public interface HistoryMetadataManager
{
    /**
     * Persists the HistoryMetadata related to the change group. Does not check permissions for the entity being
     * stored.
     *
     * @param changeGroupId the id of the changegroup to save the metadata for
     * @param user the user that performed the change, null if anonymous
     * @param historyMetadata the metadata to persist
     * @since JIRA 6.3
     */
    void saveHistoryMetadata(@Nonnull final Long changeGroupId, @Nullable final ApplicationUser user, @Nonnull final HistoryMetadata historyMetadata);

    /**
     * Retrieves the HistoryMetadata related to the ChangeHistory.
     *
     * @param changeHistory the change history to get the metadata for
     * @param user the user retrieving the information (requires the VIEW_ISSUE permission for the associated issue), null if anonymous
     * @return a HistoryMetadataResult with either the HistoryMetadata, or errors
     * @since JIRA 6.3
     */
    HistoryMetadataResult getHistoryMetadata(@Nonnull final ChangeHistory changeHistory, @Nullable final ApplicationUser user);

    /**
     * Retrieves the HistoryMetadata releated to the ChangeHistory with the given id. Doesn't check any permissions
     * @param changeHistoryId the changehistory id to get the metadata for
     * @return a HistoryMetadataResult with either the HistoryMetadata, or empty
     * @since JIRA 6.3
     */
    public HistoryMetadataManager.HistoryMetadataResult getHistoryMetadata(long changeHistoryId);

    /**
     * Holds the information about retrieving history metadata, or errors encountered
     */
    @ExperimentalApi
    public static class HistoryMetadataResult extends ServiceResultImpl
    {
        @Nullable
        private final HistoryMetadata historyMetadata;

        @Internal
        HistoryMetadataResult(@Nonnull final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.historyMetadata = null;
        }

        @Internal
        HistoryMetadataResult(@Nullable HistoryMetadata historyMetadata)
        {
            super(new SimpleErrorCollection());
            this.historyMetadata = historyMetadata;
        }

        /**
         * @return the retrieved history metadata, null if not defined
         */
        public @Nullable HistoryMetadata getHistoryMetadata()
        {
            return historyMetadata;
        }
    }
}
