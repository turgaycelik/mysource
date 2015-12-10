package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.MockUser;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;

public class TestIsIssueEditableCondition
{
    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueManager issueManager = mocksControl.createMock(IssueManager.class);

        final User fred = new MockUser("fred");

        expect(issueManager.isEditable(issue)).andReturn(false);

        final AbstractIssueCondition condition = new IsIssueEditableCondition(issueManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueManager issueManager = mocksControl.createMock(IssueManager.class);

        expect(issueManager.isEditable(issue)).andReturn(false);

        final AbstractIssueCondition condition = new IsIssueEditableCondition(issueManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }


}
