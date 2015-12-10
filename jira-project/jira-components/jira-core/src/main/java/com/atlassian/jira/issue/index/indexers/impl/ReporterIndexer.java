package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import java.util.Locale;

public class ReporterIndexer extends UserFieldIndexer
{
    public ReporterIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forReporter().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forReporter().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexUserkeyWithDefault(doc, getDocumentFieldId(), issue.getReporterId(), SystemSearchConstants.forReporter().getEmptyIndexValue(), issue);
    }
}
