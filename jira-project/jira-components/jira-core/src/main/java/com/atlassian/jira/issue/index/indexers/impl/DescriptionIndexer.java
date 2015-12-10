package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class DescriptionIndexer extends BaseFieldIndexer
{
    public DescriptionIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forDescription().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forDescription().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        String descValue = issue.getDescription();
        indexText(doc, getDocumentFieldId(), descValue, issue);
        indexText(doc, PhraseQuerySupportField.forIndexField(getDocumentFieldId()), descValue, issue);
        indexTextForSorting(doc, DocumentConstants.ISSUE_SORT_DESC, descValue, issue);
    }
}
