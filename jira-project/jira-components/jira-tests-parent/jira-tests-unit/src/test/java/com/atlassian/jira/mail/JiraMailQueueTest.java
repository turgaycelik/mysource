package com.atlassian.jira.mail;

import com.atlassian.jira.util.BaseUrl;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.base.Function;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class JiraMailQueueTest
{
    public static final String DYNAMIC_URL = "base";
    public static final String STATIC_URL = "static";

    private final MockBaseUrl baseUrl = new MockBaseUrl(DYNAMIC_URL, STATIC_URL);

    @Mock
    private MailQueue delegate;

    private JiraMailQueue jmq;

    @Before
    public void before()
    {
         jmq = new JiraMailQueue(delegate, baseUrl);
    }

    @Test
    public void sizeDelegates()
    {
        when(delegate.size()).thenReturn(15);
        assertThat(jmq.size(), Matchers.equalTo(15));
    }

    @Test
    public void errorSizeDelegates()
    {
        when(delegate.errorSize()).thenReturn(31);
        assertThat(jmq.errorSize(), Matchers.equalTo(31));
    }

    @Test
    public void addItemDelegates()
    {
        final MockMailQueueItem item = new MockMailQueueItem();
        jmq.addItem(item);
        verify(delegate).addItem(item);
    }

    @Test
    public void addErrorItemDelegates()
    {
        final MockMailQueueItem item = new MockMailQueueItem();
        jmq.addErrorItem(item);
        verify(delegate).addErrorItem(item);
    }

    @Test
    public void getQueueDelegates()
    {
        final Queue<MailQueueItem> queueItems = new LinkedList<MailQueueItem>();
        when(delegate.getQueue()).thenReturn(queueItems);
        assertThat(jmq.getQueue(), Matchers.sameInstance(queueItems));
    }

    @Test
    public void getErrorQueueDelegates()
    {
        final Queue<MailQueueItem> queueItems = new LinkedList<MailQueueItem>();
        when(delegate.getErrorQueue()).thenReturn(queueItems);
        assertThat(jmq.getErrorQueue(), Matchers.sameInstance(queueItems));
    }

    @Test
    public void isSendingDelegates()
    {
        assertThat(jmq.isSending(), Matchers.equalTo(false));
        verify(delegate).isSending();
    }

    @Test
    public void getSendingStartedDelegates()
    {
        Timestamp timestamp = new Timestamp(20L);
        when(delegate.getSendingStarted()).thenReturn(timestamp);
        assertThat(jmq.getSendingStarted(), Matchers.equalTo(timestamp));
    }

    @Test
    public void emptyErrorQueueDelegates()
    {
        jmq.emptyErrorQueue();
        verify(delegate).emptyErrorQueue();
    }

    @Test
    public void resendErrorQueueDelegates()
    {
        jmq.resendErrorQueue();
        verify(delegate).resendErrorQueue();
    }

    @Test
    public void unStickDelegates()
    {
        jmq.unstickQueue();
        verify(delegate).unstickQueue();
    }

    @Test
    public void getItemBeingSentDelegates()
    {
        final MockMailQueueItem item = new MockMailQueueItem();

        when(delegate.getItemBeingSent()).thenReturn(item);
        assertThat(jmq.getItemBeingSent(), Matchers.<MailQueueItem>sameInstance(item));
    }

    @Test
    public void sendBufferUsesStaticBaseUrl()
    {
        final JiraMailQueue jiraMailQueue = new JiraMailQueue(new MockMailQueue(), baseUrl);

        final MockMailQueueItem item = new MockMailQueueItem();
        jiraMailQueue.addItem(item);
        jiraMailQueue.sendBuffer();

        //Just make sure the URL is what we expect to be before the mail is sent.
        assertThat(baseUrl.getBaseUrl(), Matchers.equalTo(DYNAMIC_URL));

        //The static URL should have been used during the send.
        assertThat(item.baseUrlAtSend, Matchers.equalTo(STATIC_URL));

        //Make sure the dynamic URL has been restored.
        assertThat(baseUrl.getBaseUrl(), Matchers.equalTo(DYNAMIC_URL));
    }

    public class MockMailQueueItem implements MailQueueItem
    {
        private String baseUrlAtSend;

        @Override
        public void send()
        {
            baseUrlAtSend = baseUrl.getBaseUrl();
        }

        @Override
        public String getSubject()
        {
            return "Subject";
        }

        @Override
        public Date getDateQueued()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int getSendCount()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean hasError()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int compareTo(final MailQueueItem o)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void execute()
        {
            send();
        }
    }

    public static class MockBaseUrl implements BaseUrl
    {
        private String currentBaseUrl;
        private final String staticBaseUrl;

        public MockBaseUrl(final String baseUrl, final String staticBaseUrl)
        {
            this.staticBaseUrl = staticBaseUrl;
            this.currentBaseUrl = baseUrl;
        }

        @Nonnull
        @Override
        public String getBaseUrl()
        {
            return currentBaseUrl;
        }

        @Nonnull
        @Override
        public String getCanonicalBaseUrl()
        {
            return currentBaseUrl;
        }

        @Nullable
        @Override
        public <I, O> O runWithStaticBaseUrl(@Nullable final I input, @Nonnull final Function<I, O> runnable)
        {
            String lastUrl = currentBaseUrl;
            currentBaseUrl = staticBaseUrl;
            try
            {
                return runnable.apply(input);
            }
            finally
            {
                currentBaseUrl = lastUrl;
            }
        }
    }


    public static class MockMailQueue implements MailQueue
    {
        private Queue<MailQueueItem> items = new LinkedList<MailQueueItem>();
        private Queue<MailQueueItem> errorItems = new LinkedList<MailQueueItem>();

        @Override
        public void sendBuffer()
        {
            MailQueueItem item;
            while ((item = items.poll()) != null)
            {
                try
                {
                    item.send();
                }
                catch (MailException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public int size()
        {
            return items.size();
        }

        @Override
        public int errorSize()
        {
            return errorItems.size();
        }

        @Override
        public void addItem(final MailQueueItem item)
        {
            items.add(item);
        }

        @Override
        public void addErrorItem(final MailQueueItem item)
        {
            errorItems.add(item);
        }

        @Override
        public Queue<MailQueueItem> getQueue()
        {
            return items;
        }

        @Override
        public Queue<MailQueueItem> getErrorQueue()
        {
            return errorItems;
        }

        @Override
        public boolean isSending()
        {
            return false;
        }

        @Override
        public Timestamp getSendingStarted()
        {
            return null;
        }

        @Override
        public void emptyErrorQueue()
        {
            errorItems.clear();
        }

        @Override
        public void resendErrorQueue()
        {
            items.addAll(errorItems);
            emptyErrorQueue();
        }

        @Override
        public MailQueueItem getItemBeingSent()
        {
            return null;
        }

        @Override
        public void unstickQueue()
        {
        }
    }
}