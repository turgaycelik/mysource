package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class ResolutionIndexer extends BaseFieldIndexer
{
    public ResolutionIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forResolution().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forResolution().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        if (issue.getResolutionObject() != null)
        {
            indexKeyword(doc, getDocumentFieldId(), issue.getResolutionObject().getId(), issue);
        }
        else
        {
            indexKeyword(doc, getDocumentFieldId(), NO_VALUE_INDEX_VALUE, issue);
        }
    }
}
