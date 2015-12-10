package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class FixForVersionsIndexer extends BaseFieldIndexer
{
    public FixForVersionsIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forFixForVersion().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forFixForVersion().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexDependentEntities(issue, doc, IssueRelationConstants.FIX_VERSION, getDocumentFieldId());
    }
}
