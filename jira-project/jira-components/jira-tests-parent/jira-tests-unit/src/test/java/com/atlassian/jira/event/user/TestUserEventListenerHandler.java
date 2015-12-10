package com.atlassian.jira.event.user;

import java.util.List;

import com.atlassian.event.spi.ListenerInvoker;
import com.atlassian.jira.local.MockControllerTestCase;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.1
 */
public class TestUserEventListenerHandler extends MockControllerTestCase
{
    @Test
    public void testGetsIssueEventListnerForIssueEvent() throws Exception
    {
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        final UserEventListenerHandler userEventListenerHandler = new UserEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = userEventListenerHandler.getInvokers(listener);
        assertEquals(1, invokers.size());
        assertTrue(invokers.get(0) instanceof UserEventListenerHandler.UserEventInvoker);
    }

    @Test
    public void testGetsIssueEventListnerForNonIssueEvent() throws Exception
    {
        final UserEventListenerHandler userEventListenerHandler = new UserEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = userEventListenerHandler.getInvokers(new Object());
        assertEquals(0, invokers.size());
    }
    
}
