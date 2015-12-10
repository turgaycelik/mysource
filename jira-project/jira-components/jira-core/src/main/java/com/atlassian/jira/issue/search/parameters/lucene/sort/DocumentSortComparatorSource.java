package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.Issue;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import java.io.IOException;
import java.util.Arrays;

/**
 * This uses an Issue Sort Comparator to retrive the issue and then  calls {@link IssueSortComparator#compare(Issue, Issue)}
 * for each document it encounters.
 * <p/>
 * Whilst slower than the MappedSortComparator, it is used by some custom field implementations.
 */
public class DocumentSortComparatorSource extends FieldComparatorSource
{
    private final IssueSortComparator sortComparator;

    public DocumentSortComparatorSource(IssueSortComparator sortComparator)
    {
        this.sortComparator = sortComparator;
    }


    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException
    {
        return new InternalFieldComparator(numHits, fieldname, sortComparator);
    }

    /**
     * This compares whole issues using the Issue Comparator passed in.
     */
    public final class InternalFieldComparator extends FieldComparator
    {
        private Issue[] values;
        private final int numHits;
        private final String field;
        private IssueSortComparator sortComparator;
        private Issue bottom;
        private IndexReader reader;

        InternalFieldComparator(int numHits, String field, final IssueSortComparator sortComparator)
        {
            int initSize = Math.min(1024, numHits);
            values = new Issue[initSize];
            this.field = field;
            this.sortComparator = sortComparator;
            this.numHits = numHits;
        }

        @Override
        public int compare(int slot1, int slot2)
        {
            final Issue v1 = values[slot1];
            final Issue v2 = values[slot2];
            if (v1 == null)
            {
                if (v2 == null)
                {
                    return 0;
                }
                return 1;
            }
            else if (v2 == null)
            {
                return -1;
            }
            return sortComparator.compare(v1, v2);
        }

        @Override
        public int compareBottom(int doc) throws IOException
        {
            final Issue v2 = sortComparator.getIssueFromDocument(reader.document(doc));
            if (bottom == null)
            {
                if (v2 == null)
                {
                    return 0;
                }
                return 1;
            }
            else if (v2 == null)
            {
                return -1;
            }
            return sortComparator.compare(bottom, v2);
        }

        @Override
        public void copy(int slot, int doc) throws IOException
        {
            ensureCapacity(slot);
            values[slot] = sortComparator.getIssueFromDocument(reader.document(doc));
        }

        private void ensureCapacity(int slot)
        {
            if (values.length <= slot)
            {
                int newSize = Math.min(numHits, values.length * 2);
                if (newSize <= slot)  // Just to be really sure we don't blow up here.
                {
                    newSize = slot + 1;
                }
                values = Arrays.copyOf(values, newSize);
            }
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase)
        {
            this.reader = reader;
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

        final DocumentSortComparatorSource that = (DocumentSortComparatorSource) o;

        return (sortComparator != null ? sortComparator.equals(that.sortComparator) : that.sortComparator == null);

    }

    public int hashCode()
    {
        return (sortComparator != null ? sortComparator.hashCode() : 0);
    }
}
