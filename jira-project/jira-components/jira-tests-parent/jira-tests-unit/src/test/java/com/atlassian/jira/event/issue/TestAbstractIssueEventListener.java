package com.atlassian.jira.event.issue;

import com.atlassian.jira.issue.Issue;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestAbstractIssueEventListener
{
    private AbstractIssueEventListenerSpy listener;

    @Before
    public void setUp()
    {
        listener = new AbstractIssueEventListenerSpy();
    }

    @Test
    public void issueCreatedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueCreated(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueUpdatedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueUpdated(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueAssignedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueAssigned(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueResolvedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueResolved(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueClosedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueClosed(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueCommentedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueCommented(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueCommentEditedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueCommentEdited(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueCommentDeletedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueCommentDeleted(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueWorklogUpdatedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueWorklogUpdated(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueWorklogDeletedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueWorklogDeleted(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueReopenedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueReopened(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueDeletedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueDeleted(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueWorkloggedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueWorkLogged(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueStartedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueStarted(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueStoppedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueStopped(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueMovedCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueMoved(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void issueGenericEventCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.issueGenericEvent(event);

        assertThat(listener.event, is(event));
    }

    @Test
    public void customEventCallsHandleIssueEvent()
    {
        IssueEvent event = issueEvent();

        listener.customEvent(event);

        assertThat(listener.event, is(event));
    }

    private IssueEvent issueEvent()
    {
        return new IssueEvent(null, null, null, null);
    }

    private static class AbstractIssueEventListenerSpy extends AbstractIssueEventListener
    {
        public IssueEvent event = null;

        @Override
        protected void handleDefaultIssueEvent(final IssueEvent event)
        {
            this.event = event;
        }
    }
}
