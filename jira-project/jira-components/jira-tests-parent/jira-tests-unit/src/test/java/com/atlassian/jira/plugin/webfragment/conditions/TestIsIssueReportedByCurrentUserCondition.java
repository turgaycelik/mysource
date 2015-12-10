package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.MockUser;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestIsIssueReportedByCurrentUserCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final User fred = new MockUser("fred");

        expect(issue.getReporterId()).andReturn("fred");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullReporter()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final User fred = new MockUser("fred");

        expect(issue.getReporterId()).andReturn(null);

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullCurrent()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        expect(issue.getReporterId()).andReturn("fred");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final User fred = new MockUser("fred");

        expect(issue.getReporterId()).andReturn("admin");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();


    }

}
