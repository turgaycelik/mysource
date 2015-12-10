package com.atlassian.jira.issue.statistics.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.IndexSearcher;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

public class FieldHitCollector extends FieldableDocumentHitCollector
{
    private List<String> values = new ArrayList<String>();
    private final String fieldName;
    private final FieldSelector fieldSelector;

    public FieldHitCollector(IndexSearcher searcher, final String fieldName)
    {
        this(fieldName);
    }

    public FieldHitCollector(final String fieldName)
    {
        this.fieldName = notBlank("fieldName", fieldName);
        fieldSelector = new MapFieldSelector(fieldName);
    }

    public void collect(Document d)
    {
        values.add(d.get(getFieldName()));
    }

    public List<String> getValues()
    {
        return values;
    }

    private String getFieldName()
    {
        return fieldName;
    }

    @Override
    protected FieldSelector getFieldSelector()
    {
        return fieldSelector;
    }
}
