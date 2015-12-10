package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class IssueIdIndexer extends BaseFieldIndexer
{
    public IssueIdIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return "issueid";
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forIssueId().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexKeyword(doc, getDocumentFieldId(), String.valueOf(issue.getId()), issue);
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }
}
