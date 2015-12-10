package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlassian.blobstore.client.api.Access;
import io.atlassian.blobstore.client.api.BlobStoreService;
import io.atlassian.blobstore.client.api.Failure;
import io.atlassian.blobstore.client.api.GetResult;
import io.atlassian.blobstore.client.api.HeadResult;
import io.atlassian.blobstore.client.api.PutResult;
import io.atlassian.blobstore.client.api.Unit;

/**
 * Implementation of an attachment store that communicates with the Blobstore
 *
 * @since v6.3
 */
public class BlobStoreAttachmentStore implements RemoteAttachmentStore
{
    private static final Logger log = LoggerFactory.getLogger(BlobStoreAttachmentStore.class);
    private static final String BLOBSTORE_PLUGIN_MISSING_MESSAGE = "BlobStore Client Plugin not loaded - please disable BlobStore integration by disabling feature flags com.atlassian.jira.*_ATTACHMENTS_*.";

    private BlobStoreService getBlobStore()
    {
        final BlobStoreService bss = ComponentAccessor.getOSGiComponentInstanceOfType(BlobStoreService.class);
        if (bss == null)
        {
            return FailingBlobStoreService.INSTANCE;
        }
        return bss;
    }

    public BlobStoreAttachmentStore()
    {
    }

    @Override
    public <A> Promise<A> get(final Attachment metaData, final Function<InputStream, A> inputStreamProcessor)
    {
        final Promise<A> getOp =
            getBlobStore().get(metaData.getId().toString(), Access.builder().setCacheControl(Access.CacheControl.FOREVER).build(),
                    new Function<Option<GetResult>, A>()
                    {
                        @Override
                        public A apply(final Option<GetResult> getResults)
                        {
                            // Ideally we should return a Promise<A> instead of throwing exceptions
                            // but it's Java so the Promise gets stripped away with erasure
                            if (getResults.isEmpty())
                            {
                                throw new NoAttachmentDataException("Remote blobstore couldn't provide an input stream for attachment " + metaData.getId());
                            }
                            else
                            {
                                return inputStreamProcessor.apply(getResults.get().data());
                            }
                        }
                    }
            ).flatMap(new Function<Either<Failure, A>, Promise<A>>()
        {
            @Override
            public Promise<A> apply(final Either<Failure, A> failureAEither)
            {
                return failureAEither.fold(new Function<Failure, Promise<A>>()
                {
                    @Override
                    public Promise<A> apply(final Failure failure)
                    {
                        // Promises.rejected doesn't actually use the type parameter, so just set it to null.
                        return Promises.rejected(new AttachmentReadException(
                                "Remote blobstore couldn't provide an input stream for attachment " + metaData.getId() + ": " + failure.message()), null);
                    }
                }, new Function<A, Promise<A>>()
                {
                    @Override
                    public Promise<A> apply(@Nullable final A input)
                    {
                        return Promises.promise(input);
                    }
                });
            }
        });

        return getOp;
    }

    @Override
    public Promise<Attachment> put(final Attachment metadata, final InputStream source)
    {
        final Promise<Attachment> putOperation =
                getBlobStore().put(metadata.getId().toString(), source, metadata.getFilesize()).flatMap(
                    // Convert the Either result into a suitable promise
                    new Function<Either<Failure, PutResult>, Promise<Attachment>>()
                    {
                        @Override
                        public Promise<Attachment> apply(final Either<Failure, PutResult> failurePutResultEither)
                        {
                            return failurePutResultEither.fold(new Function<Failure, Promise<Attachment>>()
                            {
                                @Override
                                public Promise<Attachment> apply(final Failure failure)
                                {
                                    return Promises.rejected(new AttachmentWriteException(failure.message()));
                                }
                            }, new Function<PutResult, Promise<Attachment>>()
                            {
                                @Override
                                public Promise<Attachment> apply(final PutResult putResult)
                                {
                                    return Promises.promise(metadata);
                                }
                            });
                        }
                    });

        return putOperation;
    }

    @Override
    public Promise<Attachment> put(final Attachment metadata, final File source)
    {
        try
        {
            return put(metadata, new FileInputStream(FileAttachments.validateFileForAttachment(metadata, source)));
        }
        catch (IOException e)
        {
            return Promises.rejected(new AttachmentWriteException(e));
        }
    }

    @Override
    public Promise<Boolean> exists(final Attachment metaData)
    {
        final Promise<Boolean> getOp =
                getBlobStore().head(metaData.getId().toString(), Access.builder().setCacheControl(Access.CacheControl.FOREVER).build())
                    .flatMap(new Function<Either<Failure, Option<HeadResult>>, Promise<Boolean>>()
                    {
                        @Override
                        public Promise<Boolean> apply(final Either<Failure, Option<HeadResult>> input)
                        {
                            return input.fold(new Function<Failure, Promise<Boolean>>()
                                              {
                                                  @Override
                                                  public Promise<Boolean> apply(final Failure input)
                                                  {
                                                      return Promises.rejected(new AttachmentReadException(
                                                              "Remote blobstore couldn't provide an input stream for attachment " + metaData.getId() + ": " + input.message()));
                                                  }
                                              },
                                    new Function<Option<HeadResult>, Promise<Boolean>>()
                                    {
                                        @Override
                                        public Promise<Boolean> apply(final Option<HeadResult> input)
                                        {
                                            return Promises.promise(input.isDefined());
                                        }
                                    }
                            );
                        }
                    });

        return getOp;
    }

    @Override
    public Promise<Unit> move(final Attachment metaData, final String newIssueKey)
    {
        // No-op in the remote attachment store since it is independant of the attachment's location.
        return Promises.promise(Unit.UNIT);
    }

    @Override
    public Promise<Unit> delete(final Attachment attachment)
    {
        return getBlobStore().delete(attachment.getId().toString()).flatMap(new Function<Either<Failure, Boolean>, Promise<Unit>>()
        {
            @Override
            public Promise<Unit> apply(final Either<Failure, Boolean> input)
            {
                return input.fold(new Function<Failure, Promise<Unit>>()
                {
                    @Override
                    public Promise<Unit> apply(final Failure input)
                    {
                        return Promises.rejected(new AttachmentDeleteException("Error deleting attachment from remote blobstore: " + input.message()));
                    }
                }, new Function<Boolean, Promise<Unit>>()
                {
                    @Override
                    public Promise<Unit> apply(final Boolean input)
                    {
                        // If there was no error, we don't care about the response as it is just deleted.
                        return Promises.promise(Unit.UNIT);
                    }
                });
            }
        });
    }

    @Override
    public Option<ErrorCollection> errors()
    {
        if (FailingBlobStoreService.INSTANCE.equals(getBlobStore()))
        {
            final ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(BLOBSTORE_PLUGIN_MISSING_MESSAGE);
            return Option.some(errors);
        }
        else
        {
            return Option.none();
        }
    }
    
    private static final class FailingBlobStoreService implements BlobStoreService
    {
        private static final BlobStoreService INSTANCE = new FailingBlobStoreService();

        private <A> Promise<A> fail()
        {
            return Promises.rejected(new UnsupportedOperationException(BLOBSTORE_PLUGIN_MISSING_MESSAGE));
        }

        @Override
        public <A> Promise<Either<Failure, A>> get(final String key, final Access options, final Function<Option<GetResult>, A> f)
        {
            return fail();
        }

        @Override
        public Promise<Either<Failure, PutResult>> put(final String key, final InputStream stream, final Long contentLength)
        {
            return fail();
        }

        @Override
        public Promise<Either<Failure, Boolean>> delete(final String key)
        {
            return fail();
        }

        @Override
        public Promise<Either<Failure, Option<HeadResult>>> head(final String key, final Access options)
        {
            return fail();
        }
    }    
}
