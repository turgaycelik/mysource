package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Date;

/**
 * @since v4.4
 */
public class LocalDateIndexer extends AbstractCustomFieldIndexer
{
    private static final Logger log = Logger.getLogger(LocalDateIndexer.class);

    public LocalDateIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField)
    {
        super(fieldVisibilityManager, customField);
    }

    @Override
    public void addDocumentFieldsSearchable(Document doc, Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(Document doc, Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        Object value = customField.getValue(issue);
        if (value instanceof Date || value == null)
        {
            Date date = (Date) value;
            try
            {
                LocalDate localDate = LocalDateFactory.from(date);
                if (date != null)
                {
                    doc.add(new Field(getDocumentFieldId(), LuceneUtils.localDateToString(localDate), Field.Store.YES, indexType));
                }
                if (indexType == Field.Index.NOT_ANALYZED_NO_NORMS)
                {
                    doc.add(new Field(DocumentConstants.LUCENE_SORTFIELD_PREFIX + getDocumentFieldId(), LuceneUtils.localDateToString(localDate), Field.Store.NO, indexType));
                }
            }
            catch (RuntimeException ex)
            {
                // eg LocalDateFactory.from(date); will throw IllegalArgumentException on BCE dates
                log.warn("Unable to index custom date field '" + customField.getName() + "(" + customField.getId() + ") with value: " + value);
            }
        }
    }
}
