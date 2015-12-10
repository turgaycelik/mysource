package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.io.Writer;

public abstract class IssueWriterHitCollector extends DocumentHitCollector
{
    protected final Writer writer;
    private final IssueFactory issueFactory;

    public IssueWriterHitCollector(IndexSearcher searcher, Writer writer, IssueFactory issueFactory)
    {
        super(searcher);
        this.writer = writer;
        this.issueFactory = issueFactory;
    }

    public void collect(Document d)
    {
        Issue issue = issueFactory.getIssue(d);
        try
        {
            writeIssue(issue, writer);
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }
    }

    protected abstract void writeIssue(Issue issue, Writer writer) throws IOException;
}
