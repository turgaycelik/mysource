package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.Test;

import io.atlassian.blobstore.client.api.BlobStoreService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 *
 * @since v6.3
 */
public class TestBlobStoreAttachmentStoreHealth
{
    @Test
    public void testIsHealthyReturnsHealthy() throws Exception
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(BlobStoreService.class, mock(BlobStoreService.class)).init());
        assertFalse(out.errors().isDefined());
    }

    @Test
    public void testIsHealthyReturnsUnhealthyWhenThereIsNoBlobStoreService() throws Exception
    {
        SimpleAttachmentStore out = new BlobStoreAttachmentStore();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().init());
        assertTrue(out.errors().isDefined());
    }


}
