package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An indexer for the date custom fields
 *
 * @since v4.0
 */
public class DateCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private static final Logger log = Logger.getLogger(NumberCustomFieldIndexer.class);
    private final CustomField customField;

    public DateCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
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

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        Object value = customField.getValue(issue);
        if (value instanceof Date || value == null)
        {
            Date date = (Date) value;
            if (date != null)
            {
                if (date.getTime() > 0)
                {
                    doc.add(new Field(getDocumentFieldId(), LuceneUtils.dateToString(date), Field.Store.YES, indexType));
                }
                else
                {
                    log.warn("Unable to index custom date field '" + customField.getName() + "(" + customField.getId() + ") with value: " + date.getTime());
                }
            }
            if (indexType == Field.Index.NOT_ANALYZED_NO_NORMS)
            {
                doc.add(new Field(DocumentConstants.LUCENE_SORTFIELD_PREFIX + getDocumentFieldId(), LuceneUtils.dateToString(date), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            }
        }
    }
}
