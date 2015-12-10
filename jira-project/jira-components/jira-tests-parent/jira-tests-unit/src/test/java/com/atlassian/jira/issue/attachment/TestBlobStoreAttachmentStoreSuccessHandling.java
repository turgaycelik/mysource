package com.atlassian.jira.issue.attachment;

import java.io.File;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBlobStoreAttachmentStoreSuccessHandling
{
    private BlobStoreService mockService;

    @Before
    public void setUp()
    {
        // DIRTY DIRTY HACK TO INJECT A BlobStoreService into tests
        mockService = mock(BlobStoreService.class);

        PutResult putSuccess = PutResult.created("1");
        Either<Failure, PutResult> putFailure = Either.right(putSuccess);
        Promise<Either<Failure, PutResult>> putFailurePromise = Promises.promise(putFailure);

        when(mockService.put(anyString(), any(InputStream.class), any(Long.class))).thenReturn(putFailurePromise);
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(BlobStoreService.class, mockService).init());
    }

    @Test
    public void testSuccessOnGet() throws Exception
    {
        final File tmp = File.createTempFile("jira-blobstore", "tmp");
        Either<Failure, File> getSuccess = Either.right(tmp);
        Promise<Either<Failure, File>> getSuccessPromise = Promises.promise(getSuccess);
        when(mockService.get(anyString(), any(Access.class), any(Function.class))).thenReturn(getSuccessPromise);

        SimpleAttachmentStore store = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);

        File result = store.get(at, new Function<InputStream, File>()
        {
            @Override
            public File apply(@Nullable final InputStream input)
            {
                // This doesn't get called in this test as we are mocking out the execution of the function
                return tmp;
            }
        }).claim();
        assertEquals(tmp.getAbsolutePath(), result.getAbsolutePath());

        tmp.delete();
    }

    @Test
    public void testSuccessOnPut() throws Exception
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);
        InputStream is = mock(InputStream.class);

        out.put(at, is).claim();
    }

    @Test
    public void testSuccessOnExists() throws Exception
    {
        Either<Failure, Option<HeadResult>> getSuccess = Either.right(Option.some(HeadResult.create("ABC", 1)));
        Promise<Either<Failure, Option<HeadResult>>> getSuccessPromise = Promises.promise(getSuccess);
        when(mockService.head(anyString(), any(Access.class))).thenReturn(getSuccessPromise);

        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        Attachment at = mock(Attachment.class);
        when(at.getId()).thenReturn(1L);

        Boolean result = out.exists(at).claim();
        assertTrue(result);
    }

}
