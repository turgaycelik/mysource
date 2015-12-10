/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.util;

import java.util.Collection;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.issue.MockIssue;

import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestIssueObjectIssueIterable
{
    @Test
    public void testNullIdCollection()
    {
        try
        {
            new IssueObjectIssuesIterable(null);
            fail("Cannot operate with a null id collection");
        }
        catch (final NullPointerException yay)
        {}
    }

    @Test
    public void testToString()
    {
        final Mock mockIssueFactory = new Mock(IssueFactory.class);
        mockIssueFactory.setStrict(true);

        final Collection<Issue> issues = Lists.newArrayList(makeIssue(1, "TST-1"), makeIssue(1, "TST-10"), makeIssue(1, "ABC-24"), makeIssue(1, "TST-100"), makeIssue(1, "ANO-3"));

        final IssueObjectIssuesIterable issueIterable = new IssueObjectIssuesIterable(issues);
        assertEquals(IssueObjectIssuesIterable.class.getName() + " (5 items): [TST-1, TST-10, ABC-24, TST-100, ANO-3]", issueIterable.toString());

        mockIssueFactory.verify();
    }

    private static Issue makeIssue(final long id, final String key)
    {
        final MutableIssue result = new MockIssue(new Long(id));
        result.setKey(key);
        return result;
    }
}
