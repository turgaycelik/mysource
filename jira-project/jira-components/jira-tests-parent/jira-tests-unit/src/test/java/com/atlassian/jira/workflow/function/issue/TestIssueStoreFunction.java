/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.Issue;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestIssueStoreFunction
{

    private IssueStoreFunction isf;

    @Before
    public void before()
    {
        isf = new IssueStoreFunction();
    }

    @Test
    public void testIssueStoreFunctionNoIssue()
    {
        Map input = new HashMap();
        input.put("issue", null);

        isf.execute(input, null, null);
    }

    @Test
    public void testIssueStoreFunction() throws GenericEntityException
    {
        Issue issue = mock(Issue.class);
        Map input = ImmutableMap.of("issue", issue);

        isf.execute(input, null, null);

        verify(issue).store();
    }
}
