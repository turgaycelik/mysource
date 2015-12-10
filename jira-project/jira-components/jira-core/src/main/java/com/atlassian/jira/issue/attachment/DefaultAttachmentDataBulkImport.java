package com.atlassian.jira.issue.attachment;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.BoundedExecutorServiceWrapper;
import com.atlassian.jira.util.CallableFunction;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.ExceptionPolicy;

import com.atlassian.fugue.Pair;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.util.ConsumeAllAttachmentKeys;
import com.atlassian.util.concurrent.Promise;

import com.google.common.annotations.VisibleForTesting;

public class DefaultAttachmentDataBulkImport implements AttachmentDataBulkImport
{
    private final BulkAttachmentOperations bulkAttachmentOperations;
    private final AttachmentStore attachmentStore;
    private final IssueManager issueManager;

    public DefaultAttachmentDataBulkImport(final BulkAttachmentOperations operations,
            final AttachmentStore store, final IssueManager im) {
        bulkAttachmentOperations = operations;
        attachmentStore = store;
        issueManager = im;
    }

    @Override
    public void importAttachmentDataFrom(
            final ReadOnlyFileBasedAttachmentStore source,
            final int concurrency,
            final Option<Effect<Attachment>> onCompleteAttachment) throws AttachmentRuntimeException
    {
        final BoundedExecutorServiceWrapper executor =
                new BoundedExecutorServiceWrapper.Builder()
                        .withConcurrency(concurrency)
                        .withThreadPoolName("import-attachment").build();
        importAttachmentDataFrom(source, executor, onCompleteAttachment);
    }

    @VisibleForTesting
    void importAttachmentDataFrom(
            final ReadOnlyFileBasedAttachmentStore source,
            final BoundedExecutorServiceWrapper executor,
            final Option<Effect<Attachment>> onCompleteAttachment) throws AttachmentRuntimeException
    {
        final AtomicReference<Option<Throwable>> abortCause = new AtomicReference<Option<Throwable>>(Option.<Throwable>none());

        final CallableFunction<Pair<Attachment, AttachmentKey>, Void> consumer =
                new CallableFunction<Pair<Attachment, AttachmentKey>, Void>(
                        new Function<Pair<Attachment, AttachmentKey>, Void>()
                        {
                            @Override
                            public Void get(final Pair<Attachment, AttachmentKey> input)
                            {
                                final Promise<Attachment> op =
                                        attachmentStore.putAttachment(input.left(), source.getAttachmentFile(input.right()))
                                            .fail(new Effect<Throwable>()
                                            {
                                                @Override
                                                public void apply(final Throwable throwable)
                                                {
                                                    abortCause.set(Option.some(throwable));
                                                }
                                            });

                                onCompleteAttachment.foreach(new com.atlassian.fugue.Effect<Effect<Attachment>>()
                                {
                                    @Override
                                    public void apply(final Effect<Attachment> attachmentEffect)
                                    {
                                        op.done(attachmentEffect);
                                    }
                                });
                                return null;
                            }
                        }, ExceptionPolicy.Policies.THROW);

        try
        {
            ConsumeAllAttachmentKeys.getAttachmentsWithKeys(bulkAttachmentOperations.getAllAttachments(), issueManager).foreach(
                    new Consumer<Pair<Attachment, AttachmentKey>>()
                    {
                        @Override
                        public void consume(@Nonnull final Pair<Attachment, AttachmentKey> pair)
                        {
                            Option<Throwable> currentAbortCause = abortCause.get();
                            if (currentAbortCause.isEmpty())
                            {
                                executor.submit(consumer.apply(pair));
                            }
                            else
                            {
                                // Abort has occurred, stop processing!
                                Throwable throwable = currentAbortCause.get();
                                if (throwable instanceof AttachmentRuntimeException)
                                {
                                    throw (AttachmentRuntimeException)throwable;
                                }
                                else
                                {
                                    throw new AttachmentRuntimeException(throwable);
                                }
                            }
                        }
                    }
            );
        }
        finally
        {
            executor.awaitTermination();
        }
    }
}
