/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueReindexFunction
{
    @AvailableInContainer
    @org.mockito.Mock
    private IssueIndexManager issueIndexManager;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Test
    public void testReindexIssueFunction() throws GenericEntityException, IndexException
    {
        Issue issueObject = mock(Issue.class);

        Map input = EasyMap.build("issue", issueObject);
        IssueReindexFunction irf = new IssueReindexFunction();
        irf.execute(input, null, null);

        verify(issueIndexManager).reIndex(issueObject);
    }
}
