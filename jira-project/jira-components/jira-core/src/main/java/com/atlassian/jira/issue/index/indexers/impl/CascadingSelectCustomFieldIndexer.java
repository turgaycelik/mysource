package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.statistics.CascadingSelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for the cascading select custom fields.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class CascadingSelectCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    public static final String CHILD_INDEX_SUFFIX = ":" + CascadingSelectCFType.CHILD_KEY;
    public static final String PARENT_AND_CHILD_INDEX_SUFFIX = CascadingSelectStatisticsMapper.SUB_VALUE_SUFFIX;
    public static final String PARENT_AND_CHILD_INDEX_SEPARATOR = CascadingSelectStatisticsMapper.PARENT_AND_CHILD_INDEX_SEPARATOR;


    private final CustomField customField;

    ///CLOVER:OFF

    public CascadingSelectCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
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
        final Object value = customField.getValue(issue);
        if (value instanceof Map)
        {
            final Map<String, Option> cascadingOptions = (Map<String, Option>) value;
            indexParentField(cascadingOptions, doc, indexType);
            indexChildField(cascadingOptions, doc, indexType);
            indexParentAndChildField(cascadingOptions, doc, indexType);
        }
    }

    private void indexParentField(final Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType)
    {
        final Option value = cascadingOptions.get(CascadingSelectCFType.PARENT_KEY);
        if (value != null)
        {
            addField(doc, getDocumentFieldId(), value.getOptionId().toString(), indexType);
        }
    }

    private void indexChildField(final Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType)
    {
        final Option value = cascadingOptions.get(CascadingSelectCFType.CHILD_KEY);
        if (value != null)
        {
            final String indexFieldName = getDocumentFieldId() + CHILD_INDEX_SUFFIX;
            addField(doc, indexFieldName, value.getOptionId().toString(), indexType);
        }
    }

    private void indexParentAndChildField(final Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType)
    {
        final Option parentValue = cascadingOptions.get(CascadingSelectCFType.PARENT_KEY);
        final Option childValue = cascadingOptions.get(CascadingSelectCFType.CHILD_KEY);

        if (parentValue != null)
        {
            String combinedStringValue = null;
            if (childValue != null)
            {
                combinedStringValue = parentValue.getOptionId().toString() + PARENT_AND_CHILD_INDEX_SEPARATOR
                        + childValue.getOptionId().toString();
            }
            else
            {
                combinedStringValue = parentValue.getOptionId().toString() + PARENT_AND_CHILD_INDEX_SEPARATOR;
            }
            final String indexFieldName = getDocumentFieldId() + PARENT_AND_CHILD_INDEX_SUFFIX;
            addField(doc, indexFieldName, combinedStringValue, indexType);
        }
    }

    private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType)
    {
        doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
    }

    ///CLOVER:ON
}
