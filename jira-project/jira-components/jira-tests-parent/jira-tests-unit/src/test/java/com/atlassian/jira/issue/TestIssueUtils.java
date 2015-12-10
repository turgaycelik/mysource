/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.Predicate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestIssueUtils
{
    @Test
    public void shouldFilterWithPredicate() throws Exception
    {
        Collection<Issue> issues = new ArrayList<Issue>(Arrays.<Issue>asList(new MockIssue(1L), new MockIssue(2L)));
        Predicate<Issue> filter = new Predicate<Issue>()
        {
            public boolean evaluate(final Issue input)
            {
                return !input.getId().equals(1L);
            }
        };

        IssueUtils.filterIssues(issues, filter);

        assertEquals(1, issues.size());
        assertEquals(new Long(1), issues.iterator().next().getId());
    }
}
