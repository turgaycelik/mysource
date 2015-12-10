package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Set;

/**
 * Responsible for adding fields to the Issue document being indexed.
 *
 * @since 4.2
 */
public class LabelsIndexer extends BaseFieldIndexer
{
    public static final String NO_VALUE_INDEX_VALUE = FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE;

    public LabelsIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forLabels().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return DocumentConstants.ISSUE_LABELS_FOLDED;
    }

    public void addIndex(Document doc, Issue issue)
    {
        final Set<Label> labels = issue.getLabels();
        final Field.Index index = unanalyzed(issue);
        if (labels == null || labels.isEmpty())
        {
            //only index the empty value if the field is actually shown.
            if(!index.equals(Field.Index.NO))
            {
                doc.add(new Field(getDocumentFieldId(), NO_VALUE_INDEX_VALUE, Field.Store.NO, index));
            }
        }
        else
        {
            for (Label label : labels)
            {
                String theLabel = label.getLabel();
                if (StringUtils.isNotBlank(theLabel))
                {
                    doc.add(new Field(DocumentConstants.ISSUE_LABELS, theLabel, Field.Store.YES, index));
                    if(!index.equals(Field.Index.NO))
                    {
                        doc.add(new Field(getDocumentFieldId(), theLabel.toLowerCase(), Field.Store.NO, index));
                    }
                }
            }
        }
    }
}