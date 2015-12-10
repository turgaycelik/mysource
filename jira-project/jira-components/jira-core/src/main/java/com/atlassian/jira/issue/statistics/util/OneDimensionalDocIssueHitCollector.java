package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.statistics.StatsGroup;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Collection;

/**
 * A HitCollector that accesses the document directly to get the values for a field.  This HitCollector has low memory
 * usage (it iterates over the documents as neccessary), and is useful when you are doing a collection where there are a
 * limited number of documents, but a large number of terms in the entire index.
 */
public class OneDimensionalDocIssueHitCollector extends AbstractOneDimensionalHitCollector
{
    private final StatsGroup statsGroup;
    private final IndexReader searcher;
    private final IssueFactory issueFactory;

    public OneDimensionalDocIssueHitCollector(final String luceneGroupField, final StatsGroup statsGroup,
            final IndexReader searcher, final IssueFactory issueFactory,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache,
            final FieldManager fieldManager, final ProjectManager projectManager)
    {
        super(luceneGroupField, fieldVisibilityManager, readerCache, fieldManager, projectManager);
        this.statsGroup = statsGroup;
        this.searcher = searcher;
        this.issueFactory = issueFactory;
    }

    protected void collectIrrelevant(final int docId)
    {
        final Document issueDocument = getDocument(docId);
        if (issueDocument != null)
        {
            final Issue issue = issueFactory.getIssue(issueDocument);
            statsGroup.addIrrelevantIssue(issue);
        }
    }

    protected void collectWithTerms(final int docId, final Collection<String> terms)
    {
        final Document issueDocument = getDocument(docId);
        if (issueDocument != null)
        {
            adjustMapForIssueKey(statsGroup, terms, issueDocument);
        }
    }

    private Document getDocument(final int docId)
    {
        try
        {
            return searcher.document(docId);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private void adjustMapForIssueKey(StatsGroup statsGroup, Collection<String> values, Document issueDocument)
    {
        final Issue issue = issueFactory.getIssue(issueDocument);

        if (values == null)
        {
            statsGroup.addValue(null, issue);
            return;
        }

        for (String value : values)
        {
            statsGroup.addValue(value, issue);
        }
    }

}
