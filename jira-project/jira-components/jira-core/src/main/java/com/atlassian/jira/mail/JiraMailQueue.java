package com.atlassian.jira.mail;

import com.atlassian.jira.util.BaseUrl;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueImpl;
import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

import java.sql.Timestamp;
import java.util.Queue;
import javax.annotation.Nullable;

/**
 * A {@link com.atlassian.mail.queue.MailQueue} that ensures the {@code baseURL} for all e-mails is correct. When
 * sending e-mails the {@code baseURL} should always use JIRA's configured URL.
 *
 * @since v6.3.1
 */
public class JiraMailQueue implements MailQueue
{
    private final MailQueue delegate;
    private final BaseUrl baseUrl;

    public JiraMailQueue(final BaseUrl baseUrl)
    {
        this(new MailQueueImpl(), baseUrl);
    }

    @VisibleForTesting
    JiraMailQueue(final MailQueue delegate, final BaseUrl baseUrl)
    {
        this.delegate = delegate;
        this.baseUrl = baseUrl;
    }

    /**
     * When sending e-mails the {@code baseURL} should always use JIRA's configured URL.
     * <p>
     * JIRA normally uses the incoming HTTP request to generate the {@code baseURL}. JIRA's configured {@code baseURL} is used
     * when no request exists (e.g. executing on a scheduled thread). This means that e-mails are normally rendered
     * using the configured static {@code baseURL} as they are sent in a scheduled task. This is the correct public URL
     * for e-mails.
     * <p>
     * It is also possible send e-mails on a thread with a request (e.g. MailQueueAdmin.jspa "Send Now", or REST send now).
     * In this case we want the {@code baseURL} to come from the configured {@code baseURL} and not from the HTTP request
     * to keep things consistent with the scheduled e-mail runner.
     */
    @Override
    public void sendBuffer()
    {
        baseUrl.runWithStaticBaseUrl(null, new Function<Void, Void>()
        {
            @Override
            public Void apply(@Nullable final Void nullValue)
            {
                delegate.sendBuffer();
                return null;
            }
        });
    }

    @Override
    public int size() {return delegate.size();}

    @Override
    public int errorSize() {return delegate.errorSize();}

    @Override
    public void addItem(final MailQueueItem item) {delegate.addItem(item);}

    @Override
    public void addErrorItem(final MailQueueItem item) {delegate.addErrorItem(item);}

    @Override
    public Queue<MailQueueItem> getQueue() {return delegate.getQueue();}

    @Override
    public Queue<MailQueueItem> getErrorQueue() {return delegate.getErrorQueue();}

    @Override
    public boolean isSending() {return delegate.isSending();}

    @Override
    public Timestamp getSendingStarted() {return delegate.getSendingStarted();}

    @Override
    public void emptyErrorQueue() {delegate.emptyErrorQueue();}

    @Override
    public void resendErrorQueue() {delegate.resendErrorQueue();}

    @Override
    public MailQueueItem getItemBeingSent() {return delegate.getItemBeingSent();}

    @Override
    public void unstickQueue() {delegate.unstickQueue();}
}
