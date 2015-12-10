package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class PriorityIndexer extends BaseFieldIndexer
{
    public PriorityIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forPriority().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forPriority().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        if (issue.getPriorityObject() != null)
        {
            indexKeyword(doc, getDocumentFieldId(), issue.getPriorityObject().getId(), issue);
        }
        else
        {
            indexKeyword(doc, getDocumentFieldId(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE, issue);
        }
    }
}
