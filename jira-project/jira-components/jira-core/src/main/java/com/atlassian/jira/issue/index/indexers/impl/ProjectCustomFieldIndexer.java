package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple custom field indexer for the ProjectPicker custom field.
 *
 * @since v4.0
 */
public class ProjectCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;
    private final ProjectConverter projectConverter;

    public ProjectCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, ProjectConverter projectConverter)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.projectConverter = projectConverter;
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
            doc.add(new Field(getDocumentFieldId(), projectConverter.getString((GenericValue) value), Field.Store.YES, indexType));
        }
    }
}
