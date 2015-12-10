package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.IndexSearcher;

import java.util.Map;

/**
 * A HitCollector that accesses the document directly to get the values for a field.  This HitCollector
 * has low memory usage (it iterates over the documents as neccessary), and is useful when you are doing
 * a collection where there are a limited number of documents, but a large number of terms in the entire index.
 */
public class OneDimensionalDocHitCollector extends FieldableDocumentHitCollector
{
    private final String luceneGroupField;
    private final Map result;
    private final FieldSelector fieldSelector;

    public OneDimensionalDocHitCollector(final String luceneGroupField, Map result, IndexSearcher searcher)
    {
        super(searcher);
        this.luceneGroupField = luceneGroupField;
        this.result = result;
        fieldSelector = new MapFieldSelector(luceneGroupField);
    }

    @Override
    protected FieldSelector getFieldSelector()
    {
        return fieldSelector;

    }

    public void collect(Document d)
    {
        String[] values = d.getValues(luceneGroupField);
        adjustMapForValues(result, values);
    }

    private void adjustMapForValues(Map map, String[] values)
    {
        if (values == null)
            return;

        for (String value : values)
        {
            Integer count = (Integer) map.get(value);

            if (count == null)
            {
                count = new Integer(0);
            }

            map.put(value, new Integer(count.intValue() + 1));
        }
    }
}
