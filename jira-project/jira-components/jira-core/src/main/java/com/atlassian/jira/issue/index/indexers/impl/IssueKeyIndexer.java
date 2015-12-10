package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;

import java.util.Locale;

public class IssueKeyIndexer extends BaseFieldIndexer
{
    public IssueKeyIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    public String getId()
    {
        return SystemSearchConstants.forIssueKey().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forIssueKey().getIndexField();
    }

    public void addIndex(final Document doc, final Issue issue)
    {
        if ((issue != null) && !StringUtils.isBlank(issue.getKey()))
        {
            final String key = issue.getKey();
            indexKeyword(doc, DocumentConstants.ISSUE_KEY, key, issue);

            //We are using Locale.ENGLISH to ensure there is consistent mapping rules. Sorry to people from Turkey.
            indexFoldedKeyword(doc, getDocumentFieldId(), key, Locale.ENGLISH, issue);

            final long numPart = IssueKey.from(key).getIssueNumber();
            if (numPart >= 0)
            {
                indexLongAsKeyword(doc, DocumentConstants.ISSUE_KEY_NUM_PART, numPart, issue);

                //This is the field that we can do JQL like issueKey >= JRA-1330.
                doc.add(new Field(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE, NumberTools.longToString(numPart), Field.Store.NO,
                    Field.Index.NOT_ANALYZED_NO_NORMS));
            }
        }
    }
}
