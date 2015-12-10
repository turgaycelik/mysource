package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

/**
 * Class used for indexing the CreatorSystemField
 *
 * @since v6.2
 */
public class CreatorIndexer extends UserFieldIndexer
{
    public CreatorIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forCreator().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forCreator().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexUserkeyWithDefault(doc, getDocumentFieldId(), issue.getCreatorId(), SystemSearchConstants.forCreator().getEmptyIndexValue(), issue);
    }
}
