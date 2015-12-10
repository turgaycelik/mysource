package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nonnull;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import io.atlassian.blobstore.client.api.Unit;

/**
 * Represents the most fundamental functionality of an attachment store - streams.
 *
 * This may eventually replace AttachmentStore.
 *
 * @since v6.3
 */
public interface SimpleAttachmentStore extends AttachmentHealth
{
    /**
     * Store attachment data for a given attachment.
     *
     * @param metaData attachment metadata, used to determine the logical key under which to store the attachment data
     * @param data source data. The attachment store will close this stream when it has completed.
     * The stream will be closed once the operation is complete.
     * @return A promise of an attachment that performs the 'put' operation once the promise is claimed. The promise will
     * contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    @Nonnull
    Promise<Attachment> put(@Nonnull Attachment metaData, @Nonnull InputStream data);

    /**
     * Store attachment data for a given attachment.
     *
     * @param metaData attachment metadata, used to determine the logical key under which to store the attachment data
     * @param data source data. It is assumed that the file will exist during the attachment process (i.e. relatively
     * long lived).
     * @return A promise of an attachment that performs the 'put' operation once the promise is claimed.
     */
    @Nonnull
    Promise<Attachment> put(@Nonnull Attachment metaData, @Nonnull File data);

    /**
     * Retrieve data for a given attachment.
     * @param metaData attachment metadata, used to determine the logical key under which to store the attachment data
     * @param inputStreamProcessor Function that processes the attachment data. This function MUST clean up upon failure of reading from
     * the input stream, and must support being re-executed. e.g. If the function writes out to a temp file, the temp file should be
     * created by the function.
     * @param <A> The class that the inputStreamProcessor returns when run.
     * @return A promise of an object that represented the processed attachment data (i.e. from running the inputStreamProcessor over the
     * attachment data). The promise will contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    <A> Promise<A> get(final Attachment metaData, final Function<InputStream, A> inputStreamProcessor);

    /**
     * Returns true if the attachment exists in the store.
     *
     * @param metaData attachment metadata, used to determine the logical key under which to store the attachment data
     * @return a promise that when claimed will return true if the attachment exists in the store.  The promise will
     * contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    Promise<Boolean> exists(Attachment metaData);

    /**
     * Delete the specified attachment.
     *
     * @param attachment The attachment to delete.
     * @return a promise that contains an AttachmentCleanupException in case of error.
     */
    Promise<Unit> delete(Attachment attachment);

    /**
     * Moves an attachment from its current issue under a new one
     * @param metaData attachment metadata, used to determine the logical key of the attachment to be moved.
     * @param newIssueKey the key of the new issue under which the attachment will reside.
     * @return a promise that will be completed when the operation is complete. It will
     * contain an {@link com.atlassian.jira.issue.attachment.AttachmentRuntimeException} in case of error.
     */
    Promise<Unit> move(Attachment metaData, String newIssueKey);
}
