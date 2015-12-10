package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class IssueTypeIndexer extends BaseFieldIndexer
{
    public IssueTypeIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forIssueType().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forIssueType().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    public void addIndex(Document doc, Issue issue)
    {
        IssueType issueTypeObject = issue.getIssueTypeObject();
        if (issueTypeObject != null) // this should only be null in tests
        {
            indexKeyword(doc, getDocumentFieldId(), issueTypeObject.getId(), issue);
        }
    }
}
