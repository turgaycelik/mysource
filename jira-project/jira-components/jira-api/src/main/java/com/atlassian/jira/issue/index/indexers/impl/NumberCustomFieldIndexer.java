package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple custom field indexer for the number custom fields
 *
 * @since v4.0
 */
public class NumberCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;
    private final DoubleConverter doubleConverter;

    public NumberCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final DoubleConverter doubleConverter)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.doubleConverter = notNull("doubleConverter", doubleConverter);
        this.customField = customField;
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
        Object value = customField.getValue(issue);
        if (value != null)
        {
            final String string = doubleConverter.getStringForLucene((Double) value);
            doc.add(new Field(getDocumentFieldId(), string, Field.Store.YES, indexType));
        }
    }
}