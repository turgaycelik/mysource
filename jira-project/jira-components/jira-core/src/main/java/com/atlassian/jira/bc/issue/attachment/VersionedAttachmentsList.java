package com.atlassian.jira.bc.issue.attachment;

import com.atlassian.jira.issue.attachment.Attachment;

import java.util.List;

/**
 * Represents a list of attachments that contains several versions of the same file.
 *
 * This class is responsible for determining what attachments represent a different version of the same file,
 * and offering clients methods to query version information for attachments.
 *
 * @since v4.2
 */
public interface VersionedAttachmentsList
{
    /**
     * Returns the underlying list of attachments.
     * @return A {@link java.util.List} of {@link com.atlassian.jira.issue.attachment.Attachment}.
     */
    List<Attachment> asList();

    /**
     * Determines whether the specified attachment represents the latest version of the corresponding file.
     * @param attachment The attachment in play.
     * @return true if this is the latest (or only) version of the file in question; otherwise false.
     */
    boolean isLatestVersion(Attachment attachment);
}
