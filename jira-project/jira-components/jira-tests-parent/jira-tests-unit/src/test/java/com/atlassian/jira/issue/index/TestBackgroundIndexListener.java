package com.atlassian.jira.issue.index;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestBackgroundIndexListener
{

    private BackgroundIndexListener listener;
    private Issue issue1 = new MockIssue(1, "HSP-1");
    private Issue issue2 = new MockIssue(2, "HSP-2");
    private Issue issue3 = new MockIssue(3, "HSP-3");
    private Issue issue4 = new MockIssue(4, "HSP-4");
    private Issue issue5 = new MockIssue(5, "HSP-5");
    private Issue issue6 = new MockIssue(6, "HSP-6");
    private Issue issue7 = new MockIssue(7, "HSP-7");

    @Before
    public void setup()
    {
        listener = new BackgroundIndexListener();
    }

    @Test
    public void testIssueUpdated()
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_UPDATED_ID);
        listener.issueUpdated(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueAssigned() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_ASSIGNED_ID);
        listener.issueAssigned(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueResolved() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_RESOLVED_ID);
        listener.issueResolved(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueClosed() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_CLOSED_ID);
        listener.issueClosed(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueCommented() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_COMMENTED_ID);
        listener.issueCommented(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueCommentEdited() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_COMMENT_EDITED_ID);
        listener.issueCommentEdited(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueCommentDeleted() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_COMMENT_DELETED_ID);
        listener.issueCommentDeleted(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueWorklogUpdated() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_WORKLOG_UPDATED_ID);
        listener.issueWorklogUpdated(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueWorklogDeleted() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_WORKLOG_DELETED_ID);
        listener.issueWorklogDeleted(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueReopened() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_REOPENED_ID);
        listener.issueReopened(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueDeleted() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_DELETED_ID);
        listener.issueDeleted(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(0));
        assertThat(listener.getDeletedIssues().size(), is(1));
        assertThat(listener.getDeletedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
    }

    @Test
    public void testIssueWorkLogged() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_WORKLOGGED_ID);
        listener.issueWorkLogged(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testIssueMoved() throws Exception
    {
        IssueEvent event = new IssueEvent(issue1, null, null, EventType.ISSUE_MOVED_ID);
        listener.issueMoved(event);
        assertThat(listener.getTotalModifications(), is(1));
        assertThat(listener.getUpdatedIssues().size(), is(1));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1)));
        assertThat(listener.getDeletedIssues().size(), is(0));
    }

    @Test
    public void testMultipleUpdates() throws Exception
    {
        listener.issueUpdated(new IssueEvent(issue1, null, null, EventType.ISSUE_UPDATED_ID));
        listener.issueUpdated(new IssueEvent(issue2, null, null, EventType.ISSUE_UPDATED_ID));
        listener.issueMoved(new IssueEvent(issue1, null, null, EventType.ISSUE_MOVED_ID));
        listener.issueAssigned(new IssueEvent(issue2, null, null, EventType.ISSUE_ASSIGNED_ID));
        listener.issueResolved(new IssueEvent(issue3, null, null, EventType.ISSUE_RESOLVED_ID));
        listener.issueDeleted(new IssueEvent(issue4, null, null, EventType.ISSUE_DELETED_ID));
        listener.issueDeleted(new IssueEvent(issue4, null, null, EventType.ISSUE_DELETED_ID));
        listener.issueDeleted(new IssueEvent(issue5, null, null, EventType.ISSUE_DELETED_ID));
        listener.issueUpdated(new IssueEvent(issue6, null, null, EventType.ISSUE_UPDATED_ID));

        assertThat(listener.getTotalModifications(), is(6));
        assertThat(listener.getUpdatedIssues().size(), is(4));
        assertThat(listener.getUpdatedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), Long.valueOf(6)));
        assertThat(listener.getDeletedIssues().size(), is(2));
        assertThat(listener.getDeletedIssues(), IterableMatchers.hasItems(Long.class, Long.valueOf(4), Long.valueOf(5)));
    }

}
