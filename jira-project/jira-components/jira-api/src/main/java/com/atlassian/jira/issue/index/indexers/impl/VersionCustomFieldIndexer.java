package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple version custom field indexer.
 *
 * @since v4.0
 */
public class VersionCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;

    public VersionCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
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

    public void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        final Object value = customField.getValue(issue);
        if (value != null && value instanceof Collection)
        {
            // NOTE: we make the assumption here that the value returned from the custom field is a collection of versions
            // because that is what is returned by VersionCFType#getValue()
            Collection<Version> versions = (Collection<Version>) value;
            for (final Version version : versions)
            {
                doc.add(new Field(getDocumentFieldId(), version.getId().toString(), Field.Store.YES, indexType));
            }
        }
    }
}
