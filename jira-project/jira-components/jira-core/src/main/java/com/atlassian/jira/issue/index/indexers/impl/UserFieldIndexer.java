package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

/**
 * Abstract FieldIndexer that has helper methods to index usernames in a case-insensitive manner consistent with what
 * Crowd Embedded does.
 *
 * @since v5.0
 */
public abstract class UserFieldIndexer extends BaseFieldIndexer
{
    protected UserFieldIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    protected void indexUserKey(final Document doc, final String indexField, final String userkey, final Issue issue)
    {
        indexKeyword(doc, indexField, userkey, issue);
    }

    /**
     * Index a single userkey field (case intact), with a default if the field is not set
     *
     */
    protected void indexUserkeyWithDefault(final Document doc, final String indexField, final String userkey, final String defaultValue, final Issue issue)
    {
        indexKeywordWithDefault(doc, indexField, userkey, defaultValue, issue);
    }

}
