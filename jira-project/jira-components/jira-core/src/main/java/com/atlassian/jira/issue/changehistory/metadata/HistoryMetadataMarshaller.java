package com.atlassian.jira.issue.changehistory.metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows converting the HistoryMetadata object to and from the representation that's stored in the database
 *
 * @since JIRA 6.3
 */
public interface HistoryMetadataMarshaller
{
    /**
     * @param input a String representation of HistoryMetadata created by this marshaller
     * @return a HistoryMetadata object based on it's string representation, or null if the representation is invalid
     */
    public @Nullable HistoryMetadata unmarshall(@Nonnull String input);

    /**
     * @param historyMetadata a HistoryMetadata object to marshall
     * @return a string representation of the HistoryMetadata object
     */
    public String marshall(@Nonnull HistoryMetadata historyMetadata);
}
