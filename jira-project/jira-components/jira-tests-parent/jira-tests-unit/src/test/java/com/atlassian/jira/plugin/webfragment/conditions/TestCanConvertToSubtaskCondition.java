package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCanConvertToSubtaskCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueToSubTaskConversionService conversionService = mocksControl.createMock(IssueToSubTaskConversionService.class);

        JiraServiceContext context = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());

        expect(conversionService.canConvertIssue(context, issue)).andReturn(true);

        final AbstractIssueCondition condition = new CanConvertToSubTaskCondition(conversionService);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueToSubTaskConversionService conversionService = mocksControl.createMock(IssueToSubTaskConversionService.class);

        JiraServiceContext context = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());

        expect(conversionService.canConvertIssue(context, issue)).andReturn(false);

        final AbstractIssueCondition condition = new CanConvertToSubTaskCondition(conversionService);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }
}
