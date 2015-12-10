package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.search.IssueComparator;
import org.apache.lucene.document.Document;

public class DefaultIssueSortComparator implements IssueSortComparator
{
    private final IssueComparator issueComparator;
    private final IssueFactory issueFactory;

    public DefaultIssueSortComparator(IssueComparator issueComparator)
    {
        this(issueComparator, ComponentAccessor.getComponentOfType(IssueFactory.class));
    }

    public DefaultIssueSortComparator(IssueComparator issueComparator, IssueFactory issueFactory)
    {
        if (issueComparator == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires an instance of " + IssueComparator.class.getName());
        }
        this.issueComparator = issueComparator;

        if (issueFactory == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires an instance of " + IssueFactory.class.getName());
        }
        this.issueFactory = issueFactory;
    }

    @Override
    public int compare(Issue issue1, Issue issue2)
    {
        return issueComparator.compare(issue1, issue2);
    }

    public Issue getIssueFromDocument(Document document)
    {
        return issueFactory.getIssue(document);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DefaultIssueSortComparator that = (DefaultIssueSortComparator) o;

        return (issueComparator != null ? issueComparator.equals(that.issueComparator) : that.issueComparator == null);

    }

    public int hashCode()
    {
        return (issueComparator != null ? issueComparator.hashCode() : 0);
    }

}
