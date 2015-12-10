package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import com.atlassian.core.util.FileUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.gzipfilter.util.IOUtils;

import com.atlassian.jira.event.ComponentManagerShutdownEvent;
import com.atlassian.jira.util.BoundedExecutorServiceWrapper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.AttachmentStore.AttachmentAdapter;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import org.apache.log4j.Logger;

import io.atlassian.blobstore.client.api.Unit;

/**
 * @since v6.1
 */
public final class DefaultAttachmentStore implements FileSystemAttachmentStore
{
    private static final Logger log = Logger.getLogger(DefaultAttachmentStore.class);

    private final AttachmentDirectoryAccessor directoryAccessor;
    private final ResettableLazyReference<BoundedExecutorServiceWrapper> managedExecutor;
    private final EventPublisher eventPublisher;

    public DefaultAttachmentStore(final AttachmentDirectoryAccessor da, final EventPublisher eventPublisher)
    {
        // By default we should be able to allow many concurrent operations (it's using a cached thread pool, and in most
        // cases the file system won't have timeout issues.
        this(da, eventPublisher, new BoundedExecutorServiceWrapper.Builder().withConcurrency(100).withThreadPoolName("file-attachment-store"));
    }

    @VisibleForTesting
    DefaultAttachmentStore(final AttachmentDirectoryAccessor da, final EventPublisher eventPublisher, final BoundedExecutorServiceWrapper.Builder executorBuilder)
    {
        directoryAccessor = da;
        managedExecutor = new ResettableLazyReference<BoundedExecutorServiceWrapper>()
        {
            @Override
            protected BoundedExecutorServiceWrapper create() throws Exception
            {
                return executorBuilder.build();
            }
        };
        this.eventPublisher = eventPublisher;
        eventPublisher.register(this);
    }

    /**
     * Returns the physical File for the given Attachment.
     * This method performs better as it does not need to look up the issue object.
     *
     * @param issue the issue the attachment belongs to.
     * @param attachment the attachment.
     * @return the file.
     * @throws com.atlassian.jira.exception.DataAccessException on failure getting required attachment info.
     */
    @Override
    public File getAttachmentFile(final Issue issue, final Attachment attachment) throws DataAccessException
    {
        return getAttachmentFile(attachment);
    }

    /**
     * Returns the physical File for the given Attachment.
     * If you are calling this on multiple attachments for the same issue, consider using the overridden method that
     * passes in the issue.  Else, this goes to the database for each call.
     *
     * @param attachment the attachment.
     * @return the file.
     * @throws com.atlassian.jira.exception.DataAccessException on failure getting required attachment info.
     */
    @Override
    public File getAttachmentFile(final Attachment attachment) throws DataAccessException
    {
        return FileAttachments.getAttachmentFileHolder(
                AttachmentKeys.from(attachment), directoryAccessor.getAttachmentRootPath());
    }

    /**
     * This is intended for cases where you want more control over where the attachment actually lives and you just want
     * something to handle the look up logic for the various possible filenames an attachment can have.
     * <p/>
     * In practice, this is just used during Project Import
     *
     * @param attachment it's not an attachment but it acts like one for our purposes.
     * @param attachmentDir the directory the attachments live in. This is different that the system-wide attachment
     * directory. i.e. this would "attachments/MKY/MKY-1" and not just "attachments"
     * @return the actual attachment
     */
    @Override
    public File getAttachmentFile(final AttachmentAdapter attachment, final File attachmentDir)
    {
        return FileAttachments.getAttachmentFileHolder(attachment, attachmentDir);
    }

    @Override
    public Promise<Attachment> put(final Attachment metadata, final InputStream data)
    {
        final File attachmentFile = FileAttachments.getAttachmentFileHolder(
                AttachmentKeys.from(metadata), directoryAccessor.getAttachmentRootPath());
        return managedExecutor.get().submit(new Callable<Attachment>()
        {
            @Override
            public Attachment call() throws Exception
            {
                try
                {
                    FileUtils.copyFile(data, attachmentFile, true);
                    return metadata;
                }
                catch (IOException e)
                {
                    throw new AttachmentWriteException("Could not save attachment data from stream.", e);
                }
                catch (RuntimeException e)
                {
                    throw new AttachmentWriteException(e);
                }
                finally
                {
                    IOUtils.closeQuietly(data);
                }
            }
        });
    }

    @Override
    public Promise<Attachment> put(final Attachment metadata, final File source)
    {
        final File file, attachmentFile;
        try
        {
            file = FileAttachments.validateFileForAttachment(metadata, source);
            attachmentFile = FileAttachments.getAttachmentFileHolder(
                    AttachmentKeys.from(metadata), directoryAccessor.getAttachmentRootPath());
        }
        catch (RuntimeException re)
        {
            return Promises.rejected(new AttachmentWriteException("Cannot read source file"));
        }

        if (file.equals(attachmentFile))
        {
            return Promises.promise(metadata);
        }
        else
        {
            try
            {
                return put(metadata, new FileInputStream(source));
            }
            catch (IOException e)
            {
                return Promises.rejected(new AttachmentWriteException("Cannot read source file"));
            }
        }
    }

    @Override
    public <A> Promise<A> get(final Attachment metadata, final Function<InputStream, A> inputStreamProcessor)
    {
        final File attachmentFile = getAttachmentFile(metadata);
        return managedExecutor.get().submit(new Callable<A>()
        {
            @Override
            public A call()
            {
                try
                {
                    final InputStream inputStream = new FileInputStream(attachmentFile);
                    try
                    {
                        return inputStreamProcessor.apply(inputStream);
                    }
                    catch (RuntimeException e)
                    {
                        throw new AttachmentReadException(e);
                    }
                    finally
                    {
                        org.apache.commons.io.IOUtils.closeQuietly(inputStream);
                    }
                }
                catch (FileNotFoundException e)
                {
                    throw new AttachmentReadException(e);
                }
            }
        });
    }

    @Override
    public Promise<Boolean> exists(final Attachment metaData)
    {
        final File attachmentFile = getAttachmentFile(metaData);
        return Promises.promise(attachmentFile.exists() && attachmentFile.isFile());
    }

    public Promise<Unit> delete(final Attachment attachment)
    {
        try
        {
            final File attachmentFile = getAttachmentFile(attachment);

            if (attachmentFile.exists())
            {
                attachmentFile.delete();
            }
            else
            {
                log.warn("Trying to delete non-existent attachment: [" + attachmentFile.getAbsolutePath() + "] ..ignoring");
            }
        }
        catch (DataAccessException e)
        {
            Promises.rejected(new AttachmentCleanupException(e));
        }
        return Promises.promise(Unit.UNIT);
    }

    @Override
    public Promise<Unit> deleteAttachmentContainerForIssue(final Issue issue)
    {
        Preconditions.checkNotNull(issue);
        File attachmentDir = directoryAccessor.getAttachmentDirectory(issue);
        try
        {
            org.apache.commons.io.FileUtils.deleteDirectory(attachmentDir);
            return Promises.promise(Unit.UNIT);
        }
        catch (IOException e)
        {
            return Promises.rejected(new AttachmentCleanupException(e));
        }
    }

    @Override
    public Promise<Unit> move(final Attachment metaData, final String newIssueKey)
    {
        final File originalAttachment = getAttachmentFile(metaData);
        final String fileName = originalAttachment.getName();
        final String originalFilePath = originalAttachment.getAbsolutePath();
        if (log.isDebugEnabled())
        {
            log.debug("Attachment: " + originalFilePath);
        }
        final File targetDirectoryPath = directoryAccessor.getAttachmentDirectory(newIssueKey);
        if (targetDirectoryPath == null)
        {
            return Promises.rejected(new AttachmentMoveException("Unable to create target directory for issue key " + newIssueKey));
        }
        targetDirectoryPath.mkdirs();
        if (log.isDebugEnabled())
        {
            log.debug("Attachment: " + targetDirectoryPath + File.separator + fileName);
        }
        if (originalAttachment.exists())
        {
            final File targetFile = new File(targetDirectoryPath, fileName);
            if (originalAttachment.renameTo(targetFile))
            {
                return Promises.promise(Unit.UNIT);
            }
            else
            {
                return Promises.rejected(new AttachmentMoveException("Unable to create target file " + targetFile.getAbsolutePath()));
            }
        }
        else
        {
            return Promises.rejected(new AttachmentMoveException("Could not move the attachment '" + metaData.getFilename() + "' because it does not exist at the expected location '" + originalFilePath + "'."));
        }
    }

    @Override
    public Option<ErrorCollection> errors()
    {
        return directoryAccessor.errors();
    }

    @EventListener
    public void stop(ComponentManagerShutdownEvent event)
    {
        managedExecutor.resets().get().awaitTermination();
    }
}
