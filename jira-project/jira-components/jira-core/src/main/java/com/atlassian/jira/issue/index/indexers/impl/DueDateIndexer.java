package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class DueDateIndexer extends BaseFieldIndexer
{
    public DueDateIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forDueDate().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forDueDate().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        LocalDate localDate = LocalDateFactory.from(issue.getDueDate());
        indexLocalDateField(doc, getDocumentFieldId(), localDate, issue);
    }
}
