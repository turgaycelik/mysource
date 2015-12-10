package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.Functions;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Preconditions;
import com.google.common.io.NullOutputStream;
import com.google.common.util.concurrent.FutureCallback;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import io.atlassian.blobstore.client.api.Unit;

/**
 * AttachmentStore implementation that wraps around two AttachmentStores (a file system implementation, and a remote
 * Blobstore implementation), and handles delegating to them appropriately given dark feature flags.
 *
 * @since v6.3
 */
public class DualAttachmentStore implements AttachmentStore
{
    public static final String FS_ONLY = "com.atlassian.jira.FS_ATTACHMENTS_ONLY";
    public static final String REMOTE_ONLY =  "com.atlassian.jira.REMOTE_ATTACHMENTS_ONLY";
    public static final String FS_PRIMARY = "com.atlassian.jira.FS_ATTACHMENTS_PRIMARY";
    public static final String REMOTE_PRIMARY = "com.atlassian.jira.REMOTE_ATTACHMENTS_PRIMARY";

    private static final Logger log = Logger.getLogger(DualAttachmentStore.class);

    private final FileSystemAttachmentStore fileSystemStore;
    private final RemoteAttachmentStore remoteStore;
    private final FeatureManager featureManager;
    private final AttachmentDirectoryAccessor directoryAccessor;
    private final ThumbnailAccessor thumbnailAccessor;

    public DualAttachmentStore(@Nonnull final FileSystemAttachmentStore fileSystem,
            @Nonnull final RemoteAttachmentStore remote,
            @Nonnull final FeatureManager featureManager,
            @Nonnull final AttachmentDirectoryAccessor directoryAccessor,
            @Nonnull final ThumbnailAccessor thumbnailAccessor)
    {
        this.fileSystemStore = Preconditions.checkNotNull(fileSystem);
        this.remoteStore = Preconditions.checkNotNull(remote);
        this.featureManager = Preconditions.checkNotNull(featureManager);
        this.directoryAccessor = Preconditions.checkNotNull(directoryAccessor);
        this.thumbnailAccessor = Preconditions.checkNotNull(thumbnailAccessor);
    }

    private boolean enabled(final String featureKey)
    {
        return featureManager.isEnabled(featureKey);
    }

    @Nonnull
    private SimpleAttachmentStore getPrimaryAttachmentStore()
    {
        switch(mode())
        {
            case REMOTE_ONLY:
                return remoteStore;
            case REMOTE_PRIMARY:
                return remoteStore;
            default:
                return fileSystemStore;
        }
    }

    @Nonnull
    private Option<SimpleAttachmentStore> getSecondaryAttachmentStore()
    {
        switch(mode())
        {
            case REMOTE_PRIMARY:
                return Option.<SimpleAttachmentStore>some(fileSystemStore);
            case FS_PRIMARY:
                return Option.<SimpleAttachmentStore>some(remoteStore);
            default:
                return Option.none();
        }
    }

    private Mode mode()
    {
        if (enabled(REMOTE_ONLY))
        {
            return Mode.REMOTE_ONLY;
        }
        else if (enabled(REMOTE_PRIMARY))
        {
            return Mode.REMOTE_PRIMARY;
        }
        else if (enabled(FS_PRIMARY))
        {
            return Mode.FS_PRIMARY;
        }
        else
        {
            return Mode.FS_ONLY;
        }
    }

    private boolean isFileSystemPrimary()
    {
        switch(mode())
        {
            case REMOTE_ONLY:
                return false;
            case REMOTE_PRIMARY:
                return false;
            default:
                return true;
        }
    }

    @Override
    @Nonnull
    public File getThumbnailDirectory(final @Nonnull Issue issue)
    {
        return directoryAccessor.getThumbnailDirectory(issue);
    }

    @Override
    public File getAttachmentDirectory(final @Nonnull String issueKey)
    {
        return directoryAccessor.getAttachmentDirectory(issueKey);
    }

    @Override
    public File getAttachmentDirectory(final @Nonnull Issue issue, final boolean createDirectory)
    {
        return directoryAccessor.getAttachmentDirectory(issue, createDirectory);
    }

    @Override
    public File getTemporaryAttachmentDirectory()
    {
        return directoryAccessor.getTemporaryAttachmentDirectory();
    }

    @Override
    public File getAttachmentDirectory(final @Nonnull Issue issue)
    {
        return directoryAccessor.getAttachmentDirectory(issue);
    }

    @Override
    public File getAttachmentDirectory(String attachmentDirectory, String projectKey, String issueKey)
    {
        return directoryAccessor.getAttachmentDirectory(attachmentDirectory, projectKey, issueKey);
    }

    @Override
    public void checkValidAttachmentDirectory(final Issue issue) throws AttachmentException
    {
        directoryAccessor.checkValidAttachmentDirectory(issue);
    }

    @Override
    public void checkValidTemporaryAttachmentDirectory() throws AttachmentException
    {
        directoryAccessor.checkValidTemporaryAttachmentDirectory();
    }

    /**
     * Performs a given file retrieval on both attachment stores. This either returns the appropriate file if we are
     * in FS_ONLY or FS_PRIMARY modes, otherwise it will throw an UnsupportedOperationException because it means someone
     * has called a deprecate/soon-to-be removed method and should be told so.
     *
     * @param function The operation to perform.
     * @param <A> The return type of the operation.
     * @return The result of the function run over the file if present and allowed.
     */
    private <A> A readFromFile(final Function<FileBasedAttachmentStore, A> function)
    {
        if (isFileSystemPrimary())
        {
            return function.get(fileSystemStore);
        }
        else
        {
            throw new UnsupportedOperationException("Direct file access to attachments has been removed.");
        }
    }

    @Override
    public File getAttachmentFile(final AttachmentAdapter adapter, final File attachmentDir)
    {
        return readFromFile(new Function<FileBasedAttachmentStore, File>()
        {
            public File get(FileBasedAttachmentStore store)
            {
                return store.getAttachmentFile(adapter, attachmentDir);
            }
        });
    }

    @Override
    @Nonnull
    public File getThumbnailFile(final Attachment attachment)
    {
        return thumbnailAccessor.getThumbnailFile(attachment);
    }

    @Override
    @Nonnull
    public File getThumbnailFile(final @Nonnull Issue issue, final Attachment attachment)
    {
        return thumbnailAccessor.getThumbnailFile(issue, attachment);
    }

    @Override
    public File getLegacyThumbnailFile(final Attachment attachment)
    {
        return thumbnailAccessor.getLegacyThumbnailFile(attachment);
    }


    @Override
    public File getAttachmentFile(final Issue issue, final Attachment attachment) throws DataAccessException
    {
        return getAttachmentFile(attachment);
    }

    @Override
    public File getAttachmentFile(final Attachment attachment) throws DataAccessException
    {
        return readFromFile(new Function<FileBasedAttachmentStore, File>()
        {
            public File get(FileBasedAttachmentStore store)
            {
                return store.getAttachmentFile(attachment);
            }
        });
    }

    /**
     * Performs a write operation on underlying AttachmentStores (put or a copy).
     * If there is only one configured, then it will do it on the primary store. Otherwise, it will sequence an async
     * write to the secondary store after the write to the
     * primary. It saves the input stream data to a temporary file, and then creates input streams
     * that are used for writing to both stores.
     * @param metadata Attachment metadata
     * @param source input stream for the data to write
     * @param write Function that generates a suitable Promise<Attachment> given an underlying attachment store,
     * metadata and input stream
     * @return promise that when claimed will perform the operation.
     */
    private Promise<Attachment> doWriteOperation(
            final Attachment metadata,
            final InputStream source,
            final Function2<SimpleAttachmentStore, Pair<Attachment, InputStream>, Promise<Attachment>> write)
    {
        final Option<SimpleAttachmentStore> secondary = getSecondaryAttachmentStore();

        if (secondary.isEmpty())
        {
            return write.apply(getPrimaryAttachmentStore(), Pair.pair(metadata, source));
        }
        else
        {
            // Save the stream to a temp file so we manage the resource in here, and we don't need to do any crazy
            // syncing to keep the source open until the async write to the secondary store is finished.
            try
            {
                final File tempFile = File.createTempFile("DualAttachmentStoreTempFile", null);
                final OutputStream out = new FileOutputStream(tempFile);

                try
                {
                    IOUtils.copy(source, out);
                }
                finally
                {
                    out.close();
                }

                final InputStream primaryIs = new FileInputStream(tempFile);
                final InputStream secondaryIs = new FileInputStream(tempFile);

                return write.apply(getPrimaryAttachmentStore(), Pair.pair(metadata, primaryIs)).done(
                        new com.atlassian.util.concurrent.Effect<Attachment>()
                        {
                            @Override
                            public void apply(final Attachment attachment)
                            {
                                write.apply(secondary.get(), Pair.pair(metadata, secondaryIs)).then(new FutureCallback<Attachment>()
                                {
                                    @Override
                                    public void onSuccess(final Attachment result)
                                    {
                                        if (!tempFile.delete())
                                        {
                                            if (log.isDebugEnabled())
                                            {
                                                log.debug("Error deleting temporary file used for saving attachment: " + tempFile.getAbsolutePath());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(final Throwable t)
                                    {
                                        if (log.isDebugEnabled())
                                        {
                                            log.debug("Error writing attachment to secondary store for attachment Id:" + attachment.getId(), t);
                                        }
                                        if (!tempFile.delete())
                                        {
                                            if (log.isDebugEnabled())
                                            {
                                                log.debug("Error deleting temporary file used for saving attachment: " + tempFile.getAbsolutePath());
                                            }
                                        }
                                    }
                                });
                            }
                        }
                );
            }
            catch (IOException ioe)
            {
                return Promises.rejected(new AttachmentWriteException("Error saving attachment data to temporary file", ioe));
            }
        }
    }

    @Override
    public Promise<Attachment> putAttachment(final Attachment metadata, final InputStream source)
    {
        return doWriteOperation(metadata, source, new Function2<SimpleAttachmentStore, Pair<Attachment, InputStream>, Promise<Attachment>>()
        {
            @Override
            public Promise<Attachment> apply(final SimpleAttachmentStore attachmentStore, final Pair<Attachment, InputStream> attachmentInputStreamPair)
            {
                return attachmentStore.put(attachmentInputStreamPair.left(), attachmentInputStreamPair.right());
            }
        });
    }

    @Override
    public Promise<Attachment> putAttachment(final Attachment metadata, final File source)
    {
        final Option<SimpleAttachmentStore> secondary = getSecondaryAttachmentStore();

        if (secondary.isEmpty())
        {
            return getPrimaryAttachmentStore().put(metadata, source);
        }
        else
        {
            return getPrimaryAttachmentStore().put(metadata, source).done(
                    new com.atlassian.util.concurrent.Effect<Attachment>()
                    {
                        @Override
                        public void apply(final Attachment attachment)
                        {
                            secondary.get().put(metadata, source).fail(new com.atlassian.util.concurrent.Effect<Throwable>()
                            {
                                @Override
                                public void apply(final Throwable throwable)
                                {
                                    if (log.isDebugEnabled())
                                    {
                                        log.debug("Error writing attachment to secondary store for attachment Id:" + attachment.getId(), throwable);
                                    }
                                }
                            });
                        }
                    }
            );
        }
    }

    @Override
    public <A> Promise<A> getAttachment(final Attachment metaData, final Function<InputStream, A> inputStreamProcessor)
    {
        // The four cases are:
        // 1. FS only -> perform on primary
        // 2. Remote only -> perform on primary
        // 3. FS primary -> Run a dummy get on the remote
        // 4. Remote primary -> Check if the attachment exists locally, and if so, then return it. Otherwise get from remote
        switch(mode())
        {
            case FS_PRIMARY:
                remoteStore.get(metaData, new com.google.common.base.Function<InputStream, Integer>()
                {
                    @Override
                    public Integer apply(final InputStream input)
                    {
                        try
                        {
                            return IOUtils.copy(input, new NullOutputStream());
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeIOException(e);
                        }
                    }
                }).fail(new com.atlassian.util.concurrent.Effect<Throwable>()
                {
                    @Override
                    public void apply(final Throwable throwable)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Error reading attachment to secondary store for attachment Id:" + metaData.getId(), throwable);
                        }
                    }
                });
                return getPrimaryAttachmentStore().get(metaData, Functions.toGoogleFunction(inputStreamProcessor));

            case REMOTE_PRIMARY:
                return fileSystemStore.exists(metaData).flatMap(new com.google.common.base.Function<Boolean, Promise<A>>()
                {
                    @Override
                    public Promise<A> apply(final Boolean input)
                    {
                        if (input)
                        {
                            return fileSystemStore.get(metaData, Functions.toGoogleFunction(inputStreamProcessor));
                        }
                        else
                        {
                            return remoteStore.get(metaData, Functions.toGoogleFunction(inputStreamProcessor));
                        }
                    }
                });

            default:
                // FS and Remote only cases
                return getPrimaryAttachmentStore().get(metaData, Functions.toGoogleFunction(inputStreamProcessor));
        }
    }

    private static final com.google.common.base.Function<Unit, Void> Unit2Void =
            new com.google.common.base.Function<Unit, Void>()
    {
        @Override
        public Void apply(@Nullable final Unit unit)
        {
            return null;
        }
    };

    @Override
    public Promise<Void> move(final Attachment metaData, final String newIssueKey)
    {
        getSecondaryAttachmentStore().foreach(new Effect<SimpleAttachmentStore>()
        {
            @Override
            public void apply(final SimpleAttachmentStore store)
            {
                store.move(metaData, newIssueKey);
            }
        });
        return getPrimaryAttachmentStore().move(metaData, newIssueKey).map(Unit2Void);
    }

    @Override
    public Promise<Void> deleteAttachment(@Nonnull final Attachment attachment)
    {
        Option<SimpleAttachmentStore> secondary = getSecondaryAttachmentStore();
        if (secondary.isDefined())
        {
            secondary.get().delete(attachment).fail(new com.atlassian.util.concurrent.Effect<Throwable>()
            {
                @Override
                public void apply(final Throwable throwable)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Error deleting attachment from secondary store with Id: " + attachment.getId(), throwable);
                    }
                }
            });
        }
        return getPrimaryAttachmentStore().delete(attachment).map(Unit2Void);
    }

    @Override
    public Promise<Void> deleteAttachmentContainerForIssue(@Nonnull final Issue issue)
    {
        // This is only valid for the file system store.
        return fileSystemStore.deleteAttachmentContainerForIssue(issue).map(Unit2Void);
    }

    private enum Mode
    {
        FS_ONLY, FS_PRIMARY, REMOTE_PRIMARY, REMOTE_ONLY
    }

    @Override
    public Option<ErrorCollection> errors()
    {
        Option<ErrorCollection> primaryHealth = getPrimaryAttachmentStore().errors();
        Option<SimpleAttachmentStore> secondary = getSecondaryAttachmentStore();
        secondary.foreach(new Effect<SimpleAttachmentStore>()
        {
            @Override
            public void apply(final SimpleAttachmentStore simpleAttachmentStore)
            {
                Option<ErrorCollection> secondaryHealth = simpleAttachmentStore.errors();
                secondaryHealth.foreach(new Effect<ErrorCollection>()
                {
                    @Override
                    public void apply(final ErrorCollection errorCollection)
                    {
                        log.warn("Secondary attachment store is unhealthy: " + errorCollection.toString());
                    }
                });
            }
        });

        return primaryHealth;
    }
}
