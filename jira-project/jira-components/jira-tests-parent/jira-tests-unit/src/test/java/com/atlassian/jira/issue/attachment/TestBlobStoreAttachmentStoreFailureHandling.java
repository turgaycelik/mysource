package com.atlassian.jira.issue.attachment;

import java.io.InputStream;

import javax.annotation.Nullable;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Function;

import org.junit.Before;
import org.junit.Test;

import io.atlassian.blobstore.client.api.Access;
import io.atlassian.blobstore.client.api.BlobStoreService;
import io.atlassian.blobstore.client.api.Failure;
import io.atlassian.blobstore.client.api.HeadResult;
import io.atlassian.blobstore.client.api.PutResult;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBlobStoreAttachmentStoreFailureHandling
{
    @Before
    public void setUp()
    {
        // DIRTY DIRTY HACK TO INJECT A BlobStoreService into tests
        BlobStoreService mockService = mock(BlobStoreService.class);
        Failure testFailure = new Failure()
        {
            @Override
            public String message()
            {
                return "TEST";
            }
        };

        Either<Failure, String> getFailure = Either.left(testFailure);
        Promise<Either<Failure, String>> getFailurePromise = Promises.promise(getFailure);
        when(mockService.get(anyString(), any(Access.class), any(Function.class))).thenReturn(getFailurePromise);

        Either<Failure, PutResult> putFailure = Either.left(testFailure);
        Promise<Either<Failure, PutResult>> putFailurePromise = Promises.promise(putFailure);

        Either<Failure, Option<HeadResult>> existsFailure = Either.left(testFailure);
        Promise<Either<Failure, Option<HeadResult>>> existsFailurePromise = Promises.promise(existsFailure);
        when(mockService.head(anyString(), any(Access.class))).thenReturn(existsFailurePromise);

        when(mockService.put(anyString(), any(InputStream.class), any(Long.class))).thenReturn(putFailurePromise);
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(BlobStoreService.class, mockService).init());
    }

    @Test
    public void testFailureOnGetReturnsFailedPromise()
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);

        try
        {
            out.get(at, new Function<InputStream, Object>()
            {
                @Override
                public Object apply(@Nullable final InputStream input)
                {
                    throw new UnsupportedOperationException("Not implemented");
                }
            }).claim();
            fail("No exception thrown");
        }
        catch (AttachmentReadException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }

    @Test
    public void testFailureOnPutReturnsFailedPromise() throws Exception
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);
        InputStream is = mock(InputStream.class);

        try
        {
            out.put(at, is).claim();
            fail("No exception thrown");
        }
        catch (AttachmentWriteException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }

    @Test
    public void testFailureOnExistsReturnsFailedPromise() throws Exception
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);

        try
        {
            out.exists(at).claim();
            fail("No exception thrown");
        }
        catch (AttachmentReadException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }

}
