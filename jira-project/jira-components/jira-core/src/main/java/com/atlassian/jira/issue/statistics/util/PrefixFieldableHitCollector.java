package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searchable;

import java.util.Collections;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A fieldable hit collector to retrieve all the terms matching a certain prefix provided.
 *
 * @since 4.2
 */
public class PrefixFieldableHitCollector extends FieldableDocumentHitCollector
{
    private final FieldSelector fieldSelector;
    private final String fieldId;
    private final String prefix;
    private final Set<String> results;

    public PrefixFieldableHitCollector(final IndexSearcher searcher, final String fieldId, final String prefix, final Set<String> results)
    {
        super(searcher);
        this.fieldId = notNull("fieldId", fieldId);
        this.prefix = notNull("prefix", prefix);
        this.results = notNull("results", results);
        this.fieldSelector = new SetBasedFieldSelector(CollectionBuilder.newBuilder(fieldId).asSet(), Collections.<String>emptySet());
    }

    @Override
    protected FieldSelector getFieldSelector()
    {
        return fieldSelector;
    }

    @Override
    public void collect(final Document doc)
    {
        final String[] values = doc.getValues(fieldId);
        for (String value : values)
        {
            if (value.startsWith(prefix))
            {
                results.add(value);
            }
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final PrefixFieldableHitCollector that = (PrefixFieldableHitCollector) o;

        if (!fieldId.equals(that.fieldId))
        {
            return false;
        }
        if (!prefix.equals(that.prefix))
        {
            return false;
        }
        if (!results.equals(that.results))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = fieldId.hashCode();
        result = 31 * result + prefix.hashCode();
        result = 31 * result + results.hashCode();
        return result;
    }
}