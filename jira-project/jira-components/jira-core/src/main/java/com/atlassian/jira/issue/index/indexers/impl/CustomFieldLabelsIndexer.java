package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Set;

/**
 * A CustomFieldLabelsIndexer
 *
 * @since v4.2
 */
public class CustomFieldLabelsIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;
    public static final String FOLDED_EXT = "_folded";

    public CustomFieldLabelsIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
    {
        super(fieldVisibilityManager, customField);
        this.customField = customField;
    }

    @Override
    public String getDocumentFieldId()
    {
        return customField.getId() + FOLDED_EXT;
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addIndex(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addIndex(doc, issue, Field.Index.NO);
    }

    private void addIndex(final Document doc, final Issue issue, final Field.Index index)
    {
        @SuppressWarnings ("unchecked")
        final Set<Label> labels = (Set<Label>) customField.getValue(issue);
        if (labels == null || labels.isEmpty())
        {
            //only index the empty value if the field is actually shown.
            if (!index.equals(Field.Index.NO))
            {
                doc.add(new Field(getDocumentFieldId(), LabelsIndexer.NO_VALUE_INDEX_VALUE, Field.Store.NO, index));
            }
        }
        else
        {
            for (Label label : labels)
            {
                String theLabel = label.getLabel();
                if (StringUtils.isNotBlank(theLabel))
                {
                    doc.add(new Field(customField.getId(), theLabel, Field.Store.YES, index));
                    if (!index.equals(Field.Index.NO))
                    {
                        doc.add(new Field(getDocumentFieldId(), theLabel.toLowerCase(), Field.Store.NO, index));
                    }
                }
            }
        }
    }

}
