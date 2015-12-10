package com.atlassian.jira.issue.managers;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentCleanupException;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.ThumbnailAccessor;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Function;

/**
 * Manager for deleting attachments for a given issue including any thumbnails for those attachments.
 *
 * @since v6.3
 */
public final class DefaultIssueAttachmentDeleteHelper implements IssueAttachmentDeleteHelper
{
    private final AttachmentManager attachmentManager;
    private final AttachmentStore attachmentStore;
    private final ThumbnailAccessor thumbnailAccessor;

    public DefaultIssueAttachmentDeleteHelper(final AttachmentManager attachmentManager, final AttachmentStore attachmentStore, final ThumbnailAccessor thumbnailAccessor)
    {
        this.attachmentManager = attachmentManager;
        this.attachmentStore = attachmentStore;
        this.thumbnailAccessor = thumbnailAccessor;
    }

    @Override
    public Promise<Void> deleteAttachmentsForIssue(final Issue issue)
    {
        if (attachmentManager.attachmentsEnabled())
        {
            return thumbnailAccessor.deleteThumbnailDirectory(issue).flatMap(new Function<Void, Promise<Void>>()
            {
                @Override
                public Promise<Void> apply(@Nullable final Void input)
                {
                    List<Attachment> attachments = attachmentManager.getAttachments(issue);
                    try
                    {
                        for (Attachment a : attachments)
                        {
                            attachmentManager.deleteAttachment(a);
                        }
                    }
                    catch (RemoveException e)
                    {
                        return Promises.rejected(new AttachmentCleanupException(e));
                    }
                    return attachmentStore.deleteAttachmentContainerForIssue(issue);
                }
            });
        }
        else
        {
            return Promises.promise(null);
        }
    }
}
