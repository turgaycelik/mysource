package com.atlassian.jira.issue.attachment;

import com.atlassian.fugue.Option;
import com.atlassian.util.concurrent.Effect;

/**
 * Contains methods for bulk importing attachment data into the attachment storage subsystem. Importantly, it assumes
 * that the Attachment metadata already exists in the database.
 *
 * @since v6.3
 */
public interface AttachmentDataBulkImport
{
    /**
     * Import attachment data from the specified file system source with specified concurrency, and an optional effect
     * that is run after each attachment is imported (e.g. to update a task progress sink). The import operation is
     * terminated if there is an error.
     * @param source The file system source of attachment data
     * @param concurrency The number of concurrent attachment imports
     * @param onCompleteAttachment Optional effect that is run after each attachment is imported.
     * @throws AttachmentRuntimeException If there was an error.
     */
    void importAttachmentDataFrom(ReadOnlyFileBasedAttachmentStore source,
            int concurrency, Option<Effect<Attachment>> onCompleteAttachment) throws AttachmentRuntimeException;
}
