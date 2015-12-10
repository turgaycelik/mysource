package com.atlassian.jira.issue.attachment;

import java.util.Arrays;
import java.util.List;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestAttachmentsCategoriser
{
    @Mock
    Attachment txtAttachment;

    @Mock
    Attachment jpgAttachment;

    @Mock
    Attachment jpgAttachmentNoThumbnail;

    @Mock
    ThumbnailManager thumbnailManager;

    @Mock
    Thumbnail jpgThumbnail;

    @Test
    public void categoriserReturnsAllAttachmentsIncludingThoseWithoutThumbnails() throws Exception
    {
        AttachmentsCategoriser categoriser = new AttachmentsCategoriser(thumbnailManager, new AttachmentsSource());

        List<Attachment> attachmentsWithThumbnails = ImmutableList.of(jpgAttachment);
        List<Attachment> attachmentsWithoutThumbnails = ImmutableList.of(txtAttachment, jpgAttachmentNoThumbnail);
        AttachmentItems items = new AttachmentItems(Arrays.asList(
                new AttachmentItem(txtAttachment, null),
                new AttachmentItem(jpgAttachment, jpgThumbnail),
                new AttachmentItem(jpgAttachmentNoThumbnail, null)
        ));

        assertThat(categoriser.itemsThatHaveThumbs().attachments(), equalTo(attachmentsWithThumbnails));
        assertThat(categoriser.itemsThatDoNotHaveThumbs().attachments(), equalTo(attachmentsWithoutThumbnails));
        assertThat(categoriser.items(), equalTo(items));
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(thumbnailManager.isThumbnailable(txtAttachment)).thenReturn(false);
        when(thumbnailManager.getThumbnail(txtAttachment)).thenReturn(null);

        when(thumbnailManager.isThumbnailable(jpgAttachment)).thenReturn(true);
        when(thumbnailManager.getThumbnail(jpgAttachment)).thenReturn(jpgThumbnail);

        when(thumbnailManager.isThumbnailable(jpgAttachmentNoThumbnail)).thenReturn(true);
        when(thumbnailManager.getThumbnail(jpgAttachmentNoThumbnail)).thenReturn(null);
    }

    private class AttachmentsSource implements AttachmentsCategoriser.Source
    {
        @Override
        public List<Attachment> getAttachments()
        {
            return ImmutableList.of(txtAttachment, jpgAttachment, jpgAttachmentNoThumbnail);
        }
    }
}
