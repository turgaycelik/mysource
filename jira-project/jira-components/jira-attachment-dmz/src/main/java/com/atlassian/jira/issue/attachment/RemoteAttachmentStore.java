package com.atlassian.jira.issue.attachment;

/**
 * Marker interface for AttachmentStore implementations that work with a remote attachment store (e.g. blobstore)
 *
 * @since v6.3
 */
public interface RemoteAttachmentStore extends SimpleAttachmentStore
{
}
