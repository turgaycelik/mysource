package com.atlassian.jira.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.notification.NotificationRecipient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;

public class TestNotificationRecipientProcessor
{
    private static final int RECIPIENT_COUNT = 10;
    private List recipients;

    @Before
    public void setUp() throws Exception
    {
        recipients = new ArrayList();
        for (int i = 0; i < RECIPIENT_COUNT; i++)
        {
            recipients.add(new NotificationRecipient("joe.somebody@some.corp.com"));
        }
    }

    @After
    public void tearDown() throws Exception
    {
        recipients = null;
    }

    @Test
    public void testProcessorAllGood() throws Exception
    {
        final AtomicInteger counter = new AtomicInteger(0);

        final NotificationRecipientProcessor processor = new NotificationRecipientProcessor(recipients)
        {
            @Override
            void processRecipient(final NotificationRecipient recipient) throws Exception
            {
                counter.incrementAndGet();
            }

            @Override
            void handleException(final NotificationRecipient recipient, final Exception ex)
            {
                Assert.fail();
            }

        };
        processor.process();

        // verify that all recipients were processed
        assertEquals(RECIPIENT_COUNT, counter.get());
    }

    @Test
    public void testProcessorAllRuntimes() throws Exception
    {
        final AtomicInteger processCounter = new AtomicInteger(0);
        final AtomicInteger logCounter = new AtomicInteger(0);

        final NotificationRecipientProcessor processor = new NotificationRecipientProcessor(recipients)
        {
            @Override
            void processRecipient(final NotificationRecipient recipient) throws Exception
            {
                processCounter.incrementAndGet();
                throw new RuntimeException("This is runtime exception");
            }

            @Override
            void handleException(final NotificationRecipient recipient, final Exception ex)
            {
                logCounter.incrementAndGet();
                Assert.assertEquals(RuntimeException.class, ex.getClass());
                Assert.assertEquals("This is runtime exception", ex.getMessage());
            }

        };
        processor.process();

        // verify that all recipients were processed and cause runtime exceptions
        assertEquals(RECIPIENT_COUNT, processCounter.get());
        assertEquals(RECIPIENT_COUNT, logCounter.get());
    }

    @Test
    public void testProcessorAllNonRuntime() throws Exception
    {
        final AtomicInteger processCounter = new AtomicInteger(0);
        final AtomicInteger logCounter = new AtomicInteger(0);

        final NotificationRecipientProcessor processor = new NotificationRecipientProcessor(recipients)
        {
            @Override
            void processRecipient(final NotificationRecipient recipient) throws Exception
            {
                processCounter.incrementAndGet();
                throw new Exception("This is regular exception");
            }

            @Override
            void handleException(final NotificationRecipient recipient, final Exception ex)
            {
                logCounter.incrementAndGet();
                Assert.assertEquals(Exception.class, ex.getClass());
                Assert.assertEquals("This is regular exception", ex.getMessage());
            }

        };
        processor.process();

        // verify that all recipients were processed and caused exceptions
        assertEquals(RECIPIENT_COUNT, processCounter.get());
        assertEquals(RECIPIENT_COUNT, logCounter.get());
    }

    @Test
    public void testProcessorAtRandom() throws Exception
    {
        final AtomicInteger counter = new AtomicInteger(0);

        final NotificationRecipientProcessor processor = new NotificationRecipientProcessor(recipients)
        {
            @Override
            void processRecipient(final NotificationRecipient recipient) throws Exception
            {
                counter.incrementAndGet();

                if (counter.get() == 3)
                {
                    throw new RuntimeException("This is runtime exception");
                }
                if (counter.get() == 7)
                {
                    throw new Exception("This is regular exception");
                }
            }

            @Override
            void handleException(final NotificationRecipient recipient, final Exception ex)
            {
                // verify that runtime exceptions are logged
                if (counter.get() == 3)
                {
                    Assert.assertEquals(RuntimeException.class, ex.getClass());
                    Assert.assertEquals("This is runtime exception", ex.getMessage());
                }
                // verify that non-runtime exceptions are logged
                else if (counter.get() == 7)
                {
                    Assert.assertEquals(Exception.class, ex.getClass());
                    Assert.assertEquals("This is regular exception", ex.getMessage());
                }
                // ensure that processing did not cause any other exceptions
                else
                {
                    Assert.fail();
                }
            }

        };
        processor.process();

        // verify that all recipients were processed
        // (no matter if thier processing caused any exceptions or not)
        assertEquals(RECIPIENT_COUNT, counter.get());
    }

}
