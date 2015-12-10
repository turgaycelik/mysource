package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class UpdatedDateIndexer extends BaseFieldIndexer
{
    public UpdatedDateIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forUpdatedDate().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forUpdatedDate().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexDateField(doc, getDocumentFieldId(), issue.getUpdated(), issue);
    }
}
