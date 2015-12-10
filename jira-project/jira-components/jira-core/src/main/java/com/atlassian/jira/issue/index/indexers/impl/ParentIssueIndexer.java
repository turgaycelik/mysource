package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class ParentIssueIndexer extends BaseFieldIndexer
{
    private final SubTaskManager subTaskManager;

    public ParentIssueIndexer(final FieldVisibilityManager fieldVisibilityManager, final SubTaskManager subTaskManager)
    {
        super(fieldVisibilityManager);
        this.subTaskManager = subTaskManager;
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public String getId()
    {
        return "parent";
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forIssueParent().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexLongAsKeyword(doc, getDocumentFieldId(), issue.getParentId(), issue);
    }
}
