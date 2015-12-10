package com.atlassian.jira.event.issue;

import java.util.List;

import com.atlassian.event.spi.ListenerInvoker;
import com.atlassian.jira.mock.MockListenerManager;
import com.atlassian.jira.mock.event.MockIssueEventListener;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.1
 */
public class TestIssueEventListenerHandler
{
    @Test
    public void testGetsIssueEventListnerForIssueEvent() throws Exception
    {
        final IssueEventListenerHandler issueEventListenerHandler = new IssueEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = issueEventListenerHandler.getInvokers(new MockIssueEventListener(new MockListenerManager()));
        assertEquals(1, invokers.size());
        assertTrue(invokers.get(0) instanceof IssueEventListenerHandler.IssueEventInvoker);
    }

    @Test
    public void testGetsIssueEventListnerForNonIssueEvent() throws Exception
    {
        final IssueEventListenerHandler issueEventListenerHandler = new IssueEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = issueEventListenerHandler.getInvokers(new Object());
        assertEquals(0, invokers.size());
    }

}
