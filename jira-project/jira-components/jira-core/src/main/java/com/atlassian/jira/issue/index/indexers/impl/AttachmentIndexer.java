package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class AttachmentIndexer extends BaseFieldIndexer
{
    private final FieldVisibilityManager fieldVisibilityManager;

    public AttachmentIndexer(FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    public String getId()
    {
        return IssueFieldConstants.ATTACHMENT;
    }

    @Override
    public String getDocumentFieldId()
    {
        return DocumentConstants.ISSUE_ATTACHMENT;
    }

    @Override
    public void addIndex(final Document doc, final Issue issue)
    {
        indexAttachment(doc, hasAttachments(issue));
    }

    private boolean hasAttachments(final Issue issue)
    {
        return issue.getAttachments() != null && !issue.getAttachments().isEmpty();
    }

    private void indexAttachment(final Document doc, final boolean hasAttachment)
    {
        doc.add(new Field(getDocumentFieldId(), String.valueOf(hasAttachment), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return fieldVisibilityManager.isFieldVisible(IssueFieldConstants.ATTACHMENT, issue);
    }
}
