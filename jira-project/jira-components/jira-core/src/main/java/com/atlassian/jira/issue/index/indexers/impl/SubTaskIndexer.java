package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.List;

public class SubTaskIndexer extends BaseFieldIndexer
{
    private final SubTaskManager subTaskManager;
    private final IssueLinkManager issueLinkManager;

    public SubTaskIndexer(final FieldVisibilityManager fieldVisibilityManager, final SubTaskManager subTaskManager,
            final IssueLinkManager issueLinkManager)
    {
        super(fieldVisibilityManager);
        this.subTaskManager = subTaskManager;
        this.issueLinkManager = issueLinkManager;
    }

    public String getId()
    {
        return IssueFieldConstants.SUBTASKS;
    }

    public String getDocumentFieldId()
    {
        return DocumentConstants.ISSUE_SUBTASKS;
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public void addIndex(Document doc, Issue issue)
    {
        // This accesses the link manager directly (instead of issue.getSubtasks()) so that we only pull out the ids.
        // This has a performance improvement
        final List<IssueLink> outwardLinks = issueLinkManager.getOutwardLinks(issue.getId());

        for (final IssueLink outwardLink : outwardLinks)
        {
            if (outwardLink.getIssueLinkType().isSubTaskLinkType())
            {
                doc.add(new Field(getDocumentFieldId(), outwardLink.getDestinationId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            }
        }
    }
}