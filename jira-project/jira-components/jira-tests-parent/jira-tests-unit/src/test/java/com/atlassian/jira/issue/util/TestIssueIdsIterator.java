package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.mock.MockIssueManager;

import org.ofbiz.core.entity.GenericValue;

public class TestIssueIdsIterator extends AbstractTestIssueIterator<IssueIdsIssueIterable>
{
    MockIssueManager issueManager = new MockIssueManager();
    IssueFactory issueFactory = new SimpleMockIssueFactory();

    @Override
    public void setUp() throws Exception
    {
        issueManager.addIssue(issue1);
        issueManager.addIssue(issue2);
    }

    @Override
    protected IssueIdsIssueIterable getIterable(final List<? extends GenericValue> issuesGVs)
    {
        final Collection<Long> ids = new ArrayList<Long>();
        for (final GenericValue element : issuesGVs)
        {
            ids.add(element.getLong("id"));
        }
        return new IssueIdsIssueIterable(ids, issueManager);
    }
}
