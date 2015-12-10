package com.atlassian.jira.dev.reference.plugin.searchers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

/**
 * A sreference class that implements a custom field indexer
 *
 * @since v4.3
 */
public class LabelsCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private CustomField field;

    public LabelsCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField field)
    {
        super(fieldVisibilityManager, field);
        this.field = field;
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
        if (value != null && value instanceof Collection)
        {
            for (Label label : (Collection<Label>)value)
            {
                 doc.add(new Field(getDocumentFieldId(), label.getLabel(), Field.Store.YES, indexType));
            }
        }
    }
}
