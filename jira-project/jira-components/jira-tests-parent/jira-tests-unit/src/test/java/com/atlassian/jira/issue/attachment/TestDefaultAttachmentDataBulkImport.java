package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.BoundedExecutorServiceWrapper;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.CollectionEnclosedIterable;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promises;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.joda.time.Duration;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.3
 */
public class TestDefaultAttachmentDataBulkImport
{
    @Test
    public void importDataRuns() throws Exception
    {
        BulkAttachmentOperations bulk = mock(BulkAttachmentOperations.class);
        AttachmentStore store = mock(AttachmentStore.class);
        IssueManager issueManager = mock(IssueManager.class);
        ReadOnlyFileBasedAttachmentStore fs = mock(ReadOnlyFileBasedAttachmentStore.class);
        TemporaryFolder tmp = new TemporaryFolder();
        File tempFile = tmp.newFile();

        AttachmentDataBulkImport importer = new DefaultAttachmentDataBulkImport(bulk, store, issueManager);

        final AtomicInteger counter = new AtomicInteger(0);
        Option<Effect<Attachment>> onComplete = Option.<Effect<Attachment>>some(new Effect<Attachment>() {

            @Override
            public void apply(final Attachment attachment)
            {
                counter.incrementAndGet();
            }
        });

        List<Attachment> attachments = Lists.<Attachment>newArrayList(
                new MockAttachment(1L, "a", 1L),
                new MockAttachment(2L, "b", 2L),
                new MockAttachment(3L, "c", 3L),
                new MockAttachment(4L, "d", 4L),
                new MockAttachment(5L, "e", 5L)
        );
        when(bulk.getAllAttachments()).thenReturn(CollectionEnclosedIterable.from(attachments));

        Project project = mock(Project.class);
        when(project.getOriginalKey()).thenReturn("PRJ");
        List<MockIssue> issues = Lists.newArrayList(
                new MockIssue(1, "PRJ-1"),
                new MockIssue(2, "PRJ-2"),
                new MockIssue(3, "PRJ-3"),
                new MockIssue(4, "PRJ-4"),
                new MockIssue(5, "PRJ-5")
        );
        for (MockIssue i: issues)
        {
            i.setProjectObject(project);
        }
        when(issueManager.getIssueObject(1L)).thenReturn(issues.get(0));
        when(issueManager.getIssueObject(2L)).thenReturn(issues.get(1));
        when(issueManager.getIssueObject(3L)).thenReturn(issues.get(2));
        when(issueManager.getIssueObject(4L)).thenReturn(issues.get(3));
        when(issueManager.getIssueObject(5L)).thenReturn(issues.get(4));

        when(fs.getAttachmentFile(any(AttachmentKey.class))).thenReturn(tempFile);
        when(store.putAttachment(any(Attachment.class), any(File.class))).thenReturn(Promises.<Attachment>promise(new MockAttachment(1L, "a", 1L)));

        importer.importAttachmentDataFrom(fs, 5, onComplete);

        assertEquals(5, counter.get());
        verify(fs, times(5)).getAttachmentFile(any(AttachmentKey.class));
    }

    @Test
    public void importDataBailsEarlyOnError() throws Exception
    {
        BulkAttachmentOperations bulk = mock(BulkAttachmentOperations.class);
        AttachmentStore store = mock(AttachmentStore.class);
        IssueManager issueManager = mock(IssueManager.class);
        ReadOnlyFileBasedAttachmentStore fs = mock(ReadOnlyFileBasedAttachmentStore.class);
        TemporaryFolder tmp = new TemporaryFolder();
        File tempFile = tmp.newFile();

        DefaultAttachmentDataBulkImport importer = new DefaultAttachmentDataBulkImport(bulk, store, issueManager);

        final AtomicInteger counter = new AtomicInteger(0);
        Option<Effect<Attachment>> onComplete = Option.<Effect<Attachment>>some(new Effect<Attachment>()
        {

            @Override
            public void apply(final Attachment attachment)
            {
                counter.incrementAndGet();
            }
        });

        List<Attachment> attachments = Lists.<Attachment>newArrayList(
                new MockAttachment(1L, "a", 1L),
                new MockAttachment(2L, "b", 2L),
                new MockAttachment(3L, "c", 3L),
                new MockAttachment(4L, "d", 4L),
                new MockAttachment(5L, "e", 5L)
        );
        when(bulk.getAllAttachments()).thenReturn(CollectionEnclosedIterable.from(attachments));

        Project project = mock(Project.class);
        when(project.getOriginalKey()).thenReturn("PRJ");
        List<MockIssue> issues = Lists.<MockIssue>newArrayList(
                new MockIssue(1, "PRJ-1"),
                new MockIssue(2, "PRJ-2"),
                new MockIssue(3, "PRJ-3"),
                new MockIssue(4, "PRJ-4"),
                new MockIssue(5, "PRJ-5")
        );
        for (MockIssue i : issues)
        {
            i.setProjectObject(project);
        }
        when(issueManager.getIssueObject(1L)).thenReturn(issues.get(0));
        when(issueManager.getIssueObject(2L)).thenReturn(issues.get(1));
        when(issueManager.getIssueObject(3L)).thenReturn(issues.get(2));
        when(issueManager.getIssueObject(4L)).thenReturn(issues.get(3));
        when(issueManager.getIssueObject(5L)).thenReturn(issues.get(4));

        when(fs.getAttachmentFile(any(AttachmentKey.class))).thenReturn(tempFile);
        when(store.putAttachment(any(Attachment.class), any(File.class))).thenReturn(Promises.<Attachment>promise(new MockAttachment(1L, "a", 1L)))
                .thenReturn(Promises.<Attachment>rejected(new AttachmentRuntimeException("TEST")));

        BoundedExecutorServiceWrapper executor = new BoundedExecutorServiceWrapper.Builder().withConcurrency(1).withShutdownTimeout(Duration.millis(1000))
                .withExecutorService(new Supplier<ListeningExecutorService>()
                {
                    @Override
                    public ListeningExecutorService get()
                    {
                        return MoreExecutors.sameThreadExecutor();
                    }
                }).build();
        try
        {
            importer.importAttachmentDataFrom(fs, executor, onComplete);
            fail("No exception thrown");
        }
        catch (AttachmentRuntimeException e)
        {
            assertTrue("Correct exception thrown", true);
            assertEquals(1, counter.get());
            verify(fs, times(2)).getAttachmentFile(any(AttachmentKey.class));
            verify(store, times(2)).putAttachment(any(Attachment.class), any(File.class));
        }

    }

    private class MockAttachment extends Attachment
    {
        private Long attachmentId;
        private String filename;
        private Long issueId;
        private MockAttachment(Long attachmentId, String filename, Long issueId)
        {
            super(null, new MockGenericValue("FileAttachment", attachmentId));
            this.attachmentId = attachmentId;
            this.filename = filename;
            this.issueId = issueId;
        }

        public Long getId()
        {
            return attachmentId;
        }

        public String getFilename()
        {
            return filename;
        }

        public Long getIssueId()
        {
            return issueId;
        }
    }
}
