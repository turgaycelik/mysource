package com.atlassian.jira.issue.attachment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Function;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.verification.VerificationMode;

import io.atlassian.blobstore.client.api.Unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDualAttachmentStore
{
    @Test
    public void testDelegateToFSOnlyWhenNoFeatureFlags() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(anyString())).thenReturn(false);
        testDelegationSingle(times(1), never(), fm);
    }

    @Test
    public void testDelegateToFSOnly() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        testDelegationSingle(times(1), never(), fm);
    }

    @Test
    public void testDelegateToRemoteOnly() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_ONLY)).thenReturn(true);
        testDelegationSingle(never(), times(1), fm);
    }

    @Test
    public void testFSPrimary() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        testDelegationDual(times(1), times(1), fm);
    }

    @Test
    public void testRemotePrimary() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        testDelegationDual(times(1), times(1), fm);
    }

    @Test
    public void testSecondaryFailureOnGetDoesntKillEverything() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        final File file = mock(File.class);
        when(fs.get(any(Attachment.class), any(Function.class))).thenReturn(Promises.promise(file));
        when(remote.get(any(Attachment.class), any(Function.class))).thenReturn(Promises.rejected(new DataAccessException("TEST")));

        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        out.getAttachment(attachment, new com.atlassian.jira.util.Function<InputStream, File>()
        {
            @Override
            public File get(final InputStream input)
            {
                return file;
            }
        }).claim();
        verify(fs).get(eq(attachment), any(Function.class));
        verify(remote).get(eq(attachment), any(Function.class));
    }

    @Test
    public void testSecondaryFailureOnPutDoesntKillEverything() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);

        AttachmentStore out = new DualAttachmentStore(fs, remote,
                fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        InputStream is = mockInputStream();

        when(fs.put(any(Attachment.class), any(InputStream.class))).thenReturn(Promises.promise(attachment));
        when(remote.put(any(Attachment.class), any(InputStream.class))).thenReturn(Promises.<Attachment>rejected(new DataAccessException("TEST")));

        out.putAttachment(attachment, is);
        verify(fs).put(eq(attachment), any(InputStream.class));
    }

    @Test
    public void testGetFSPrimaryCallsBothAttachmentStores() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);
        Attachment attachment = mock(Attachment.class);
        final File file = new TemporaryFolder().newFile();
        when(fs.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(file));
        when(remote.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(100));

        com.atlassian.util.concurrent.Function<InputStream, File> f = new com.atlassian.util.concurrent.Function<InputStream, File>()
        {
            public File get(@Nullable final InputStream input)
            {
                return file;
            }
        };

        File result = out.getAttachment(attachment, f).claim();
        assertEquals(result.getAbsolutePath(), file.getAbsolutePath());
        verify(fs).get(eq(attachment), any(Function.class));
        verify(remote).get(eq(attachment), any(Function.class));
    }

    @Test
    public void testGetRemotePrimaryFileCachedReturnsFile() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);
        Attachment attachment = mock(Attachment.class);
        final File file = new TemporaryFolder().newFile();

        when(fs.exists(eq(attachment))).thenReturn(Promises.promise(true));
        when(fs.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(file));

        com.atlassian.util.concurrent.Function<InputStream, File> f = new com.atlassian.util.concurrent.Function<InputStream, File>()
        {
            public File get(@Nullable final InputStream input)
            {
                return file;
            }
        };

        File result = out.getAttachment(attachment, f).claim();
        assertEquals(result.getAbsolutePath(), file.getAbsolutePath());
        verify(fs).exists(eq(attachment));
        verify(fs).get(eq(attachment), any(Function.class));
        verify(remote, never()).get(eq(attachment), any(Function.class));
    }

    @Test
    public void testGetRemotePrimaryFileNotCachedCallsRemote() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);
        Attachment attachment = mock(Attachment.class);
        final File file = new TemporaryFolder().newFile();

        when(fs.exists(eq(attachment))).thenReturn(Promises.promise(false));
        when(remote.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(file));

        com.atlassian.util.concurrent.Function<InputStream, File> f = new com.atlassian.util.concurrent.Function<InputStream, File>()
        {
            public File get(@Nullable final InputStream input)
            {
                return file;
            }
        };

        File result = out.getAttachment(attachment, f).claim();
        assertEquals(result.getAbsolutePath(), file.getAbsolutePath());
        verify(fs).exists(eq(attachment));
        verify(fs, never()).get(eq(attachment), any(Function.class));
        verify(remote).get(eq(attachment), any(Function.class));
    }

    @Test
    public void testGetAttachmentFileForFSOnlyWorks() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        File file = mock(File.class);
        when(fs.getAttachmentFile(attachment)).thenReturn(file);
        assertEquals(file, out.getAttachmentFile(attachment));
        verify(fs).getAttachmentFile(attachment);
    }

    @Test
    public void testGetAttachmentFileForFSPrimaryWorks() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        File file = mock(File.class);
        when(fs.getAttachmentFile(attachment)).thenReturn(file);
        assertEquals(file, out.getAttachmentFile(attachment));
        verify(fs).getAttachmentFile(attachment);
    }

    @Test
    public void testGetAttachmentFileForRemotePrimaryFails() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        try
        {
            out.getAttachmentFile(attachment);
            fail("No exception thrown");
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }
    @Test
    public void testGetAttachmentFileForRemoteOnlyFails() throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_ONLY)).thenReturn(true);
        AttachmentStore out = new DualAttachmentStore(fs, remote, fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);
        try
        {
            out.getAttachmentFile(attachment);
            fail("No exception thrown");
        }
        catch (UnsupportedOperationException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }

    @Test
    public void testRemoteOnlyFlagHasHighestPriority() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_ONLY)).thenReturn(true);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        testDelegationSingle(never(), times(1), fm);

        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(false);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        testDelegationSingle(never(), times(1), fm);

        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(false);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        testDelegationSingle(never(), times(1), fm);
    }

    @Test
    public void testRemotePrimaryFlagHasHigherPriorityThanFSFlags() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_PRIMARY)).thenReturn(true);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        testDelegationDual(times(1), times(1), fm);

        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(false);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        testDelegationDual(times(1), times(1), fm);
    }

    @Test
    public void testFSPrimaryFlagHasHigherPriorityThanFSOnlyFlag() throws Exception
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        testDelegationDual(times(1), times(1), fm);
    }

    private InputStream mockInputStream() throws Exception
    {
        return new ByteArrayInputStream("THIS IS A MOCK".getBytes("UTF-8"));
    }

    private void testDelegationSingle(VerificationMode fsVerify, VerificationMode remoteVerify, FeatureManager featureManager) throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        AttachmentStore store = new DualAttachmentStore(fs, remote,
                featureManager, directoryAccessor, thumbnailAccessor);

        Issue issue = new MockIssue(1);
        Attachment attachment = mock(Attachment.class);
        AttachmentStore.AttachmentAdapter adapter = new AttachmentAdapterImpl(1L, "");
        final File file = mock(File.class);
        when(file.delete()).thenReturn(true, false);

        com.atlassian.util.concurrent.Function<InputStream, File> f = new com.atlassian.util.concurrent.Function<InputStream, File>()
        {
            public File get(@Nullable final InputStream input)
            {
                return file;
            }
        };

        when(fs.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(file));
        when(remote.get(eq(attachment), any(Function.class))).thenReturn(Promises.promise(file));

        InputStream is = mockInputStream();

        store.getThumbnailDirectory(issue);
        verify(directoryAccessor).getThumbnailDirectory(issue);

        store.getAttachmentDirectory("");
        verify(directoryAccessor).getAttachmentDirectory("");

        store.getAttachmentDirectory(issue, false);
        verify(directoryAccessor).getAttachmentDirectory(issue, false);

        store.getTemporaryAttachmentDirectory();
        verify(directoryAccessor).getTemporaryAttachmentDirectory();

        store.getAttachmentDirectory(issue);
        verify(directoryAccessor).getAttachmentDirectory(issue);

        store.getAttachmentDirectory("", "", "");
        verify(directoryAccessor).getAttachmentDirectory("", "", "");

        store.putAttachment(attachment, is);
        verify(fs, fsVerify).put(attachment, is);
        verify(remote, remoteVerify).put(attachment, is);

        store.getThumbnailFile(attachment);
        verify(thumbnailAccessor).getThumbnailFile(attachment);

        store.getThumbnailFile(issue, attachment);
        verify(thumbnailAccessor).getThumbnailFile(issue, attachment);

        store.getLegacyThumbnailFile(attachment);
        verify(thumbnailAccessor).getLegacyThumbnailFile(attachment);

        store.checkValidAttachmentDirectory(issue);
        verify(directoryAccessor).checkValidAttachmentDirectory(issue);

        store.checkValidTemporaryAttachmentDirectory();
        verify(directoryAccessor).checkValidTemporaryAttachmentDirectory();
    }

    private void testDelegationDual(VerificationMode fsVerify,
            VerificationMode remoteVerify,
            FeatureManager featureManager) throws Exception
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        AttachmentStore store = new DualAttachmentStore(fs, remote,
                featureManager, directoryAccessor, thumbnailAccessor);

        Issue issue = new MockIssue(1);
        Attachment attachment = mock(Attachment.class);
        AttachmentStore.AttachmentAdapter adapter = new AttachmentAdapterImpl(1L, "");
        final File file = mock(File.class);
        when(file.delete()).thenReturn(true, false);
        InputStream is = mockInputStream();

        when(fs.put(any(Attachment.class), any(InputStream.class))).thenReturn(Promises.promise(attachment));
        when(remote.put(any(Attachment.class), any(InputStream.class))).thenReturn(Promises.promise(attachment));

        store.getThumbnailDirectory(issue);
        verify(directoryAccessor).getThumbnailDirectory(issue);

        store.getAttachmentDirectory("");
        verify(directoryAccessor).getAttachmentDirectory("");

        store.getAttachmentDirectory(issue, false);
        verify(directoryAccessor).getAttachmentDirectory(issue, false);

        store.getTemporaryAttachmentDirectory();
        verify(directoryAccessor).getTemporaryAttachmentDirectory();

        store.getAttachmentDirectory(issue);
        verify(directoryAccessor).getAttachmentDirectory(issue);

        store.getAttachmentDirectory("", "", "");
        verify(directoryAccessor).getAttachmentDirectory("", "", "");

        // Input streams may be different (as we are creating new ones for the secondary).
        store.putAttachment(attachment, is);
        verify(fs, fsVerify).put(eq(attachment), any(InputStream.class));
        verify(remote, remoteVerify).put(eq(attachment), any(InputStream.class));

        store.getThumbnailFile(attachment);
        verify(thumbnailAccessor).getThumbnailFile(attachment);

        store.getThumbnailFile(issue, attachment);
        verify(thumbnailAccessor).getThumbnailFile(issue, attachment);

        store.getLegacyThumbnailFile(attachment);
        verify(thumbnailAccessor).getLegacyThumbnailFile(attachment);

        store.checkValidAttachmentDirectory(issue);
        verify(directoryAccessor).checkValidAttachmentDirectory(issue);

        store.checkValidTemporaryAttachmentDirectory();
        verify(directoryAccessor).checkValidTemporaryAttachmentDirectory();
    }

    @Test
    public void testIsHealthyFSOnly()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_ONLY)).thenReturn(true);
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("TEST");
        testIsHealthy(fm, Option.<ErrorCollection>none(), Option.some(errors), true);
        testIsHealthy(fm, Option.some(errors), Option.<ErrorCollection>none(), false);
    }

    @Test
    public void testIsHealthyRemoteOnly()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.REMOTE_ONLY)).thenReturn(true);
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("TEST");
        testIsHealthy(fm, Option.some(errors), Option.<ErrorCollection>none(), true);
        testIsHealthy(fm, Option.<ErrorCollection>none(), Option.some(errors), false);
    }

    private void testIsHealthy(FeatureManager featureManager, Option<ErrorCollection> fsResult, Option<ErrorCollection> remoteResult, boolean isHealthy)
    {
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);
        when(fs.errors()).thenReturn(fsResult);
        when(remote.errors()).thenReturn(remoteResult);
        AttachmentStore out = new DualAttachmentStore(fs, remote, featureManager, directoryAccessor, thumbnailAccessor);
        assertEquals(isHealthy, out.errors().isEmpty());
    }

    @Test
    public void testIsHealthyBothHealthy()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        testIsHealthy(fm, Option.<ErrorCollection>none(), Option.<ErrorCollection>none(), true);
    }

    @Test
    public void testIsHealthyPrimaryHealthyOnlyShouldPass()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("TEST");
        testIsHealthy(fm, Option.<ErrorCollection>none(), Option.some(errors), true);
    }

    @Test
    public void testIsHealthySecondaryHealthyOnlyShouldFail()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("TEST");
        testIsHealthy(fm, Option.some(errors), Option.<ErrorCollection>none(), false);
    }

    @Test
    public void testDeleteAttachmentDeletesFromBothStores()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);

        AttachmentStore out = new DualAttachmentStore(fs, remote,
                fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);

        when(fs.delete(eq(attachment))).thenReturn(Promises.promise(Unit.UNIT));
        when(remote.delete(eq(attachment))).thenReturn(Promises.promise(Unit.UNIT));

        out.deleteAttachment(attachment).claim();
        verify(fs).delete(eq(attachment));
        verify(remote).delete(eq(attachment));
    }

    @Test
    public void testDeleteAttachmentSecondaryFailureDoesntKillEverything()
    {
        FeatureManager fm = mock(FeatureManager.class);
        when(fm.isEnabled(DualAttachmentStore.FS_PRIMARY)).thenReturn(true);
        FileSystemAttachmentStore fs = mock(FileSystemAttachmentStore.class);
        RemoteAttachmentStore remote = mock(RemoteAttachmentStore.class);
        AttachmentDirectoryAccessor directoryAccessor = mock(AttachmentDirectoryAccessor.class);
        ThumbnailAccessor thumbnailAccessor = mock(ThumbnailAccessor.class);

        AttachmentStore out = new DualAttachmentStore(fs, remote,
                fm, directoryAccessor, thumbnailAccessor);

        Attachment attachment = mock(Attachment.class);

        when(fs.delete(eq(attachment))).thenReturn(Promises.promise(Unit.UNIT));
        when(remote.delete(eq(attachment))).thenReturn(Promises.<Unit>rejected(new AttachmentCleanupException("")));

        out.deleteAttachment(attachment).claim();
        verify(fs).delete(eq(attachment));
        verify(remote).delete(eq(attachment));
    }
}
