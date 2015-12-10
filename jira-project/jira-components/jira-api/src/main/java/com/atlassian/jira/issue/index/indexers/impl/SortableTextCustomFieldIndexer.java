package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for text fields that can be sorted
 *
 * @since v4.0
 */
public class SortableTextCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField field;
    private final String sortFieldPrefix;

    public SortableTextCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager,
            final CustomField customField, final String sortFieldPrefix)
    {
        super(fieldVisibilityManager, notNull("field", customField));
        this.sortFieldPrefix = notBlank("sortFieldPrefix", sortFieldPrefix);
        this.field = customField;
    }

    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.ANALYZED, true);
    }

    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO, false);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index fieldIndexType,
            final boolean sortable)
    {
        final Object value = field.getValue(issue);
        if (value != null)
        {
            doc.add(new Field(getDocumentFieldId(), (String) value, Field.Store.YES, fieldIndexType));
            doc.add(new Field(PhraseQuerySupportField.forIndexField(getDocumentFieldId()), (String) value, Field.Store.YES, fieldIndexType));
        }
        if (sortable)
        {
            final String valueForSorting = FieldIndexerUtil.getValueForSorting((String) value);
            doc.add(new Field(sortFieldPrefix + getDocumentFieldId(), valueForSorting, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
        }
    }
}