package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;

import org.ofbiz.core.entity.GenericValue;

public class TestIssueObjectIssuesIterator extends AbstractTestIssueIterator<IssueObjectIssuesIterable>
{
    private final IssueFactory issueFactory = new SimpleMockIssueFactory();

    @Override
    protected IssueObjectIssuesIterable getIterable(final List<? extends GenericValue> issuesGVs)
    {
        final Collection<Issue> issueObjects = new ArrayList<Issue>();
        for (final GenericValue issueGV : issuesGVs)
        {
            issueObjects.add(issueFactory.getIssue(issueGV));
        }
        return new IssueObjectIssuesIterable(issueObjects);
    }
}
