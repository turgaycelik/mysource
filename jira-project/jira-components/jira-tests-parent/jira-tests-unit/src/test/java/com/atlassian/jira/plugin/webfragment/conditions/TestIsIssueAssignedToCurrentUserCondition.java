package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestIsIssueAssignedToCurrentUserCondition
{
    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }

    @Test
    public void testTrue()
    {
        final Issue issue = mock(Issue.class);
        final ApplicationUser fred = new MockApplicationUser("Fred");

        when(issue.getAssigneeId()).thenReturn(fred.getKey());

        final AbstractIssueWebCondition condition = new IsIssueAssignedToCurrentUserCondition();
        assertTrue(condition.shouldDisplay(fred, issue, null));
    }

    @Test
    public void testNullAssignee()
    {
        final Issue issue = mock(Issue.class);
        final ApplicationUser fred = new MockApplicationUser("Fred");

        when(issue.getAssigneeId()).thenReturn(null);

        final AbstractIssueWebCondition condition = new IsIssueAssignedToCurrentUserCondition();
        assertFalse(condition.shouldDisplay(fred, issue, null));
    }

    @Test
    public void testNullCurrent()
    {
        final Issue issue = mock(Issue.class);

        when(issue.getAssigneeId()).thenReturn("fred");

        final AbstractIssueWebCondition condition = new IsIssueAssignedToCurrentUserCondition();
        assertFalse(condition.shouldDisplay(null, issue, null));
    }

    @Test
    public void testFalse()
    {
        final Issue issue = mock(Issue.class);
        final ApplicationUser fred = new MockApplicationUser("Fred");

        when(issue.getAssigneeId()).thenReturn("admin");

        final AbstractIssueWebCondition condition = new IsIssueAssignedToCurrentUserCondition();
        assertFalse(condition.shouldDisplay(fred, issue, null));
    }

}
