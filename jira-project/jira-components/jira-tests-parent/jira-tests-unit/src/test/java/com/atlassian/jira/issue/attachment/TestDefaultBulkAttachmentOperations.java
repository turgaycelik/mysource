package com.atlassian.jira.issue.attachment;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.EnclosedIterable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestDefaultBulkAttachmentOperations
{

    @Test
    public void testGetAttachmentOfIssue()
    {
        IssueManager mockIssueManager = Mockito.mock(IssueManager.class);
        OfBizDelegator mockOfBizDelegator = Mockito.mock(OfBizDelegator.class);
        DefaultBulkAttachmentOperations ops = new DefaultBulkAttachmentOperations(mockIssueManager,
                mockOfBizDelegator);

        Issue mockIssue = Mockito.mock(Issue.class);
        final Attachment mockAttachment = Mockito.mock(Attachment.class);
        Collection<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(mockAttachment);
        Mockito.when(mockIssue.getAttachments()).thenReturn(attachments);
        EnclosedIterable<Attachment> attachmentOfIssue = ops.getAttachmentOfIssue(mockIssue);
        Assert.assertEquals(attachments.size(), attachmentOfIssue.size());
        attachmentOfIssue.foreach(new Consumer<Attachment>()
        {
            @Override
            public void consume(@Nonnull final Attachment element)
            {
                Assert.assertEquals(mockAttachment, element);
            }
        });
    }
}
