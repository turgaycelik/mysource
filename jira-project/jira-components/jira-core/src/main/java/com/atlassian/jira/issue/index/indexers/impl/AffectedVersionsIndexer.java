package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class AffectedVersionsIndexer extends BaseFieldIndexer
{
    public AffectedVersionsIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forAffectedVersion().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forAffectedVersion().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexDependentEntities(issue, doc, IssueRelationConstants.VERSION, getDocumentFieldId());
    }
}
