package com.atlassian.jira.issue.managers;

import java.util.List;

import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.ThumbnailAccessor;
import com.atlassian.util.concurrent.Promises;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.3
 */
public class TestDefaultIssueAttachmentDeleteHelper
{
    @Test
    public void deleteAttachmentsForIssuesShouldWork() throws Exception
    {
        Issue issue = mock(Issue.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        when(thumbnailAccessor.deleteThumbnailDirectory(eq(issue))).thenReturn(Promises.<Void>promise(null));

        AttachmentManager attachmentManager = mock(AttachmentManager.class);
        when(attachmentManager.attachmentsEnabled()).thenReturn(true);
        Attachment a1 = mock(Attachment.class);
        Attachment a2 = mock(Attachment.class);
        List<Attachment> attachments = Lists.newArrayList(
                a1, a2
        );
        when(attachmentManager.getAttachments(eq(issue))).thenReturn(attachments);

        AttachmentStore store = mock(AttachmentStore.class);
        when(store.deleteAttachmentContainerForIssue(eq(issue))).thenReturn(Promises.<Void>promise(null));

        DefaultIssueAttachmentDeleteHelper helper = new DefaultIssueAttachmentDeleteHelper(attachmentManager, store, thumbnailAccessor);
        helper.deleteAttachmentsForIssue(issue).claim();

        verify(thumbnailAccessor).deleteThumbnailDirectory(eq(issue));
        verify(attachmentManager).deleteAttachment(eq(a1));
        verify(attachmentManager).deleteAttachment(eq(a2));
        verify(store).deleteAttachmentContainerForIssue(eq(issue));
    }
}
