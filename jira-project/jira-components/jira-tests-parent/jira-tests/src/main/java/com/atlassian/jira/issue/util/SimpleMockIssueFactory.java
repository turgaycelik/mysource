package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import org.apache.lucene.document.Document;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.mock.issue.MockIssue;

import java.util.Collection;
import java.util.List;

public class SimpleMockIssueFactory implements IssueFactory
{

    public MutableIssue getIssue()
    {
        return null;
    }

    public MutableIssue getIssue(GenericValue issueGV)
    {
        MockIssue mockIssue = new MockIssue();
        // if the GV is null - return the issue anyhow, as this is how the real issue factory works.
        if (issueGV != null)
            mockIssue.setId(issueGV.getLong("id"));
        return mockIssue;
    }

    @Override
    public MutableIssue getIssueOrNull(GenericValue issueGV)
    {
        if (issueGV == null)
            return null;
        else
            return getIssue(issueGV);
    }

    public MutableIssue cloneIssue(Issue issue)
    {
        if (issue instanceof MutableIssue)
        {
            return (MutableIssue) issue;
        }
        throw new UnsupportedOperationException();
    }

    public List getIssues(Collection issueGVs)
    {
        return null;
    }

    public Issue getIssue(Document issueDocument)
    {
        return null;
    }

    public MutableIssue cloneIssueNoParent(Issue issue)
    {
        return null;
    }
}
