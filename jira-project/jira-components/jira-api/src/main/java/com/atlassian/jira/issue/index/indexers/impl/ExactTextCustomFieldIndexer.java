package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for text fields that can be sorted
 *
 * @since v4.0
 */
public class ExactTextCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField field;

    public ExactTextCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
    {
        super(fieldVisibilityManager, notNull("field", customField));
        this.field = customField;
    }

    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        Object value = field.getValue(issue);
        final String stringValue = field.getCustomFieldType().getStringFromSingularObject(value);
        if (value != null)
        {
            doc.add(new Field(getDocumentFieldId(), stringValue, Field.Store.YES, indexType));
        }
        final String string = FieldIndexerUtil.getValueForSorting((String) value);
        doc.add(new Field(DocumentConstants.LUCENE_SORTFIELD_PREFIX + getDocumentFieldId(), string, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
    }
}