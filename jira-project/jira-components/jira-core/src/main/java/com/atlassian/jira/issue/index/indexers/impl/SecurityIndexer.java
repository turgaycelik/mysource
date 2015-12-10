package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class SecurityIndexer extends BaseFieldIndexer
{
    public SecurityIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forSecurityLevel().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forSecurityLevel().getIndexField();
    }

    // NOTE: This is here on purpose, this is so that people do not nuke all their security levels when hiding/showing
    // security levels. This does change the behaviour of how we do empty searching on security levels but this is
    // a price we are willing to pay since we are frightened of changing the security level behaviour.
    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexKeywordWithDefault(doc, getDocumentFieldId(), issue.getSecurityLevelId(), NO_VALUE_INDEX_VALUE, issue);
    }
}
