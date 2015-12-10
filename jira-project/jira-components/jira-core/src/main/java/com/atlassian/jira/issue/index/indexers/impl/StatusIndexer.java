package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class StatusIndexer extends BaseFieldIndexer
{
    public StatusIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forStatus().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forStatus().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        if (issue.getStatusObject() != null) // this should only happen in tests
        {
            indexKeyword(doc, getDocumentFieldId(), issue.getStatusObject().getId(), issue);
        }
    }
}
