package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for the multi select custom fields (e.g. check boxes).
 *
 * @since v4.0
 */
public class MultiSelectCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private static final Logger log = Logger.getLogger(MultiSelectCustomFieldIndexer.class);
    private final CustomField customField;

    ///CLOVER:OFF

    public MultiSelectCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.customField = customField;
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        try
        {
            final Object value = customField.getValue(issue);
            if (value == null)
            {
                return;
            }
            if (value instanceof Collection)
            {
                final Collection<Option> col = (Collection<Option>) value;
                for (final Option o : col)
                {
                    if (o != null)
                    {
                        final String indexValue = o.getOptionId().toString();
                        doc.add(new Field(getDocumentFieldId(), indexValue, Field.Store.YES, indexType));
                        doc.add(new Field(getDocumentFieldId()+ SelectStatisticsMapper.RAW_VALUE_SUFFIX, indexValue, Field.Store.YES, indexType));
                    }
                }
            }
            else if (value instanceof Option)
            {
                final String indexValue = ((Option) value).getOptionId().toString();
                doc.add(new Field(getDocumentFieldId(), indexValue, Field.Store.YES, indexType));
                doc.add(new Field(getDocumentFieldId()+ SelectStatisticsMapper.RAW_VALUE_SUFFIX, indexValue, Field.Store.YES, indexType));
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid custom field option");
        }
    }
    ///CLOVER:ON
}
