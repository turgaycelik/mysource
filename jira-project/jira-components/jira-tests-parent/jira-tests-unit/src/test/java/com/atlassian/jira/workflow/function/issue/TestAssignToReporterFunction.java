package com.atlassian.jira.workflow.function.issue;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableMap;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAssignToReporterFunction
{
    private User user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
    }

    @Test
    public void testBrandNewIssueDoesntStore()
    {
        AssignToReporterFunction func = new AssignToReporterFunction();

        Mock mockIssue = createMockIssue(false);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = ImmutableMap.of("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    @Test
    public void testExistingIssueDoesStore()
    {
        AssignToReporterFunction func = new AssignToReporterFunction();

        Mock mockIssue = createMockIssue(true);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = ImmutableMap.of("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    private Mock createMockIssue(boolean isCreated)
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);

        mockIssue.expectAndReturn("getReporter", user);
        mockIssue.expectVoid("setAssignee", P.args(P.eq(user)));
        mockIssue.expectAndReturn("isCreated", (isCreated ? Boolean.TRUE : Boolean.FALSE));

        if (isCreated)
        {
            mockIssue.expectVoid("store", P.ANY_ARGS);
        }
        else
        {
            mockIssue.expectNotCalled("store");
        }
        return mockIssue;
    }
}
