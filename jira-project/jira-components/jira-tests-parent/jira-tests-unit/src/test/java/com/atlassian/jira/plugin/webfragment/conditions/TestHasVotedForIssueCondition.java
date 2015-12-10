package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.user.MockUser;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHasVotedForIssueCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final User fred = new MockUser("fred");

        expect(voteManager.hasVoted(fred, issue)).andReturn(true);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullUser()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();
    }

    @Test
    public void testFalseEmpty()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final User fred = new MockUser("fred");

        expect(voteManager.hasVoted(fred, issue)).andReturn(false);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();
    }
}
