package com.atlassian.jira.web.action.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.mail.queue.MailQueue;

import org.junit.Before;
import org.junit.Test;

import webwork.action.ServletActionContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestMailQueueAdmin
{
    @Before
    public void setUp() throws Exception
    {
        ServletActionContext.setRequest(mock(HttpServletRequest.class));
        ServletActionContext.setResponse(mock(HttpServletResponse.class));
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(RedirectSanitiser.class, new MockRedirectSanitiser())
        );
    }

    @Test
    public void shouldNotFlushTheQueueIfTheFlushParameterHasNotBeenSet() throws Exception
    {
        final MailQueue mockMailQueue = mock(MailQueue.class);
        final MailQueueAdmin mailQueueAdminAction =
                new MailQueueAdmin(mockMailQueue, mock(NotificationSchemeManager.class));

        mailQueueAdminAction.execute();
        verify(mockMailQueue, never()).sendBuffer();
    }

    @Test
    public void shouldFlushTheQueueIfTheFlushParameterHasBeenSet() throws Exception
    {
        final MailQueue mockMailQueue = mock(MailQueue.class);

        final MailQueueAdmin mailQueueAdminAction =
                new MailQueueAdmin(mockMailQueue, mock(NotificationSchemeManager.class));
        mailQueueAdminAction.setFlush(true);

        mailQueueAdminAction.execute();
        verify(mockMailQueue, times(1)).sendBuffer();
    }
}
