package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.search.LuceneFieldSorter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This Sort Comparator uses a mixed strategy to retrieve the term values.  If we are retrieving less thatn 1% of the
 * documents in the index, we get each document value individually.  Once passed the 1% threshhold however we switch
 * to reading through the terms dictionary in lucene, and builds up a list of ordered terms.  It then
 * sorts the documents according to the order that they appear in the terms list.
 * This latter approach, whilst very fast, does load the entire term dictionary into memory, and allocates a slot
 * for every document value, which is why we do not use it all the time.
 *
 * We believe that most searches in very large JIRA installations
 * will return a very small portion of the document index, either because they are over only one of
 * many projects or they return only open issues, especially on dashboards and in GreenHopper and similar plugins as
 * well as in general navigator searches.
 */
public class MappedSortComparator extends FieldComparatorSource
{
    private final LuceneFieldSorter sorter;

    public MappedSortComparator(LuceneFieldSorter sorter)
    {
        this.sorter = sorter;
    }

    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException
    {
        return new InternalFieldComparator(numHits, fieldname, sorter);
    }

    public final class InternalFieldComparator extends FieldComparator
    {
        private Object[] values;
        private final int numHits;
        private final String field;
        private LuceneFieldSorter sorter;
        private Object bottom;
        private final Comparator comparator;
        private int resultsCount;
        private int fastDocThreshold;
        private ValueFinder hungryValueFinder;
        private ValueFinder lazyValueFinder;

        InternalFieldComparator(int numHits, String field, final LuceneFieldSorter sorter)
        {
            this.numHits = numHits;
            int initSize = Math.min(1024, numHits);
            this.values = new Object[initSize];
            this.field = field;
            this.sorter = sorter;
            this.comparator = this.sorter.getComparator();
        }

        @Override
        public int compare(int slot1, int slot2)
        {
            final Object v1 = values[slot1];
            final Object v2 = values[slot2];
            if (v1 == v2)
            {
                return 0;
            }
            if (v1 == null)
            {
                return 1;
            }
            else if (v2 == null)
            {
                return -1;
            }
            return comparator.compare(v1, v2);
        }

        @Override
        public int compareBottom(int doc)
        {
            final Object v2 = getDocumnetValue(doc);
            if (bottom == v2)
            {
                return 0;
            }
            if (bottom == null)
            {
                return 1;
            }
            else if (v2 == null)
            {
                return -1;
            }
            return comparator.compare(bottom, v2);
        }

        @Override
        public void copy(int slot, int doc)
        {
            ensureCapacity(slot);
            values[slot] = getDocumnetValue(doc);
        }

        private void ensureCapacity(int slot)
        {
            if (values.length <= slot)
            {
                int newSize = Math.min(numHits, values.length * 2);
                if (newSize <= slot)  // Just to re really sure we don't blow up here.
                {
                    newSize = slot + 1;
                }
                values = Arrays.copyOf(values, newSize);
            }
        }

        private Object getDocumnetValue(int doc)
        {
            // We have 2 strategies for getting the document values
            // If we get a large number of results we walk the Terms in the Index and only  convert terms to values
            // once per term and then we store these for each document.
            // If we get only a few results the above method is very wasteful of both memory and compute time.
            // Unfortunatly we don't know how many results we will get in advance so we use a pair of strategies and flip
            // strategies once we hit a reasonalbe threshhold value
            resultsCount++;
            try
            {
                if (resultsCount > fastDocThreshold)
                {
                    return hungryValueFinder.getValue(doc);
                }
                else
                {
                    return lazyValueFinder.getValue(doc);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException
        {
            resultsCount = 0;
            fastDocThreshold = reader.numDocs() / 500;
            lazyValueFinder = new LazyValueFinder(reader, field);
            hungryValueFinder = new HungryValueFinder(reader, field);
        }

        @Override
        public void setBottom(final int bottom)
        {
            this.bottom = values[bottom];
        }

        @Override
        public Comparable<?> value(int slot)
        {
            // We won't be able to pull the values from the sort
            // This is only used by org.apache.lucene.search.FiledDoc instances, which we do not use.
            return null;
        }
    }

    /**
     * This makes a call into the JiraLuceneFieldFinder to retrieve values from the Lucence index.  It returns an array
     * that is the same size as the number of documents in the reader and will have all null values if the field is not
     * present, otherwise it has the values of the field within the document.
     * <p/>
     * Broken out as package level for unit testing reasons.
     *
     * @param field the name of the field to find
     * @param reader the Lucence index reader
     * @return an non null array of values, which may contain null values.
     * @throws IOException if stuff goes wrong
     */
    Object[] getLuceneValues(final String field, final IndexReader reader) throws IOException
    {
        return JiraLuceneFieldFinder.getInstance().getCustom(reader, field, MappedSortComparator.this);
    }

    /**
     * Returns an object which, when sorted according by the comparator returned from  {@link
     * LuceneFieldSorter#getComparator()} , will order the Term values in the correct order. <p>For example, if the
     * Terms contained integer values, this method would return <code>new Integer(termtext)</code>.  Note that this
     * might not always be the most efficient implementation - for this particular example, a better implementation
     * might be to make a ScoreDocLookupComparator that uses an internal lookup table of int.
     *
     * @param termtext The textual value of the term.
     * @return An object representing <code>termtext</code> that can be sorted by {@link
     *         LuceneFieldSorter#getComparator()}
     * @see Comparable
     * @see FieldComparator
     */
    public Object getComparable(String termtext)
    {
        return sorter.getValueFromLuceneField(termtext);
    }

    public Comparator getComparator()
    {
        return sorter.getComparator();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MappedSortComparator that = (MappedSortComparator) o;

        return (sorter == null ? that.sorter == null : sorter.equals(that.sorter));
    }

    public int hashCode()
    {
        return (sorter != null ? sorter.hashCode() : 0);
    }
    
    private interface ValueFinder
    {
        Object getValue(int doc) throws IOException;
    }
    
    private class HungryValueFinder implements ValueFinder
    {
        private final IndexReader reader;
        private final String field;
        private boolean initialised = false;
        private Object[] currentDocumentValues;

        private HungryValueFinder(IndexReader reader, String field)
        {
            this.reader = reader;
            this.field = field;
        }

        @Override
        public Object getValue(int doc) throws IOException
        {
            if (!initialised)
            {
                currentDocumentValues = getLuceneValues(field, reader);
                initialised = true;
            }
            return currentDocumentValues[doc];
        }
    }
    
    private class LazyValueFinder implements ValueFinder
    {
        private final IndexReader reader;
        private final String field;
        private final FieldSelector fieldSelector;
        private final Comparator comparator = getComparator();
        private int lastDoc = -1;
        private Object lastValue = null;

        LazyValueFinder(IndexReader reader, String field)
        {
            this.reader = reader;
            this.field = field;
            fieldSelector = new FieldSelector()
            {
                @Override
                public FieldSelectorResult accept(String fieldName)
                {
                    if (LazyValueFinder.this.field.equals(fieldName))
                    {
                        return FieldSelectorResult.LOAD_AND_BREAK;
                    }
                    return FieldSelectorResult.NO_LOAD;
                }
            };
        }

        @Override
        public Object getValue(int doc)
        {
            // Once the queue is full a call to compareBottom() is immediately followed by a call to copy() for
            // qualifying results, so we do a little caching here
            if (doc == lastDoc)
            {
                return lastValue;
            }
            try
            {
                Document document = reader.document(doc, fieldSelector);
                Fieldable[] values = document.getFieldables(field);
                Object comparable = null;
                for (Fieldable field : values)
                {
                    Object value = getComparable(field.stringValue());
                    if (comparable == null || comparator.compare(value, comparable) < 1)
                    {
                        comparable = value;
                    }
                }
                lastDoc = doc;
                lastValue = comparable;
                return comparable;
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
    }
    
    
}
