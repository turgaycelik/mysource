package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple version custom field indexer for the select custom fields (e.g. radio buttons).
 *
 * @since v4.0
 */
public class SelectCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private static final Logger log = Logger.getLogger(SelectCustomFieldIndexer.class);
    private final CustomField customField;

///CLOVER:OFF

    public SelectCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
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
        final Object value;
        try
        {
            value = customField.getValue(issue);
            if (value == null)
            {
                return;
            }
            if (value instanceof Option)
            {
                String indexValue = ((Option) value).getOptionId().toString() ;
                doc.add(new Field(getDocumentFieldId(), indexValue, Field.Store.YES, indexType));
                doc.add(new Field(getDocumentFieldId() + SelectStatisticsMapper.RAW_VALUE_SUFFIX, indexValue, Field.Store.YES, indexType));
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid custom field option");
        }
    }

///CLOVER:ON
}
