package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class CreatedDateIndexer extends BaseFieldIndexer
{
    public CreatedDateIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forCreatedDate().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forCreatedDate().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexDateField(doc, getDocumentFieldId(), issue.getCreated(), issue);
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }
}
