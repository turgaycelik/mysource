package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Creates a new comparator that sorts based on UserHistory timestamp
 */
public class UserHistoryFieldComparatorSource extends FieldComparatorSource
{

    private final List<UserHistoryItem> history;

    public UserHistoryFieldComparatorSource(final List<UserHistoryItem> history)
    {
        this.history = history;
    }


    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException
    {
        return new UserHistoryFieldComparator(numHits, history);
    }

    /**
     * This compares user history timestamps.  should be lightening quick
     */
    public final class UserHistoryFieldComparator extends FieldComparator
    {
        private final Long[] values;
        private Long bottom;
        private final List<UserHistoryItem> history;

        private IndexReader reader = null;

        // This maps doc Ids to timestamps and gets regenerated everytime the reader gets swapped out. (Shouldn't be too often)
        // Damn this makes for fast sorting.
        private Map<Integer, Long> docIdToTimestampMap;


        UserHistoryFieldComparator(int numHits, final List<UserHistoryItem> history)
        {
            this.history = history;
            values = new Long[numHits];
        }

        @Override
        public int compare(int slot1, int slot2)
        {
            final Long v1 = values[slot1];
            final Long v2 = values[slot2];
            // As decending is default sort order it makes logical sense to put nulls after ones with values
            if (v1 == null)
            {
                if (v2 == null)
                {
                    return 0;
                }
                return -1;
            }
            else if (v2 == null)
            {
                return 1;
            }
            return v1.compareTo(v2);
        }

        @Override
        public int compareBottom(int doc) throws IOException
        {
            final Long v2 = docIdToTimestampMap.get(doc);
            // As decending is default sort order it makes logical sense to put nulls after ones with values
            if (bottom == null)
            {
                if (v2 == null)
                {
                    return 0;
                }
                return -1;
            }
            else if (v2 == null)
            {
                return 1;
            }
            return bottom.compareTo(v2);
        }

        @Override
        public void copy(int slot, int doc) throws IOException
        {
            values[slot] = docIdToTimestampMap.get(doc);
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException
        {
            if (this.reader != reader)
            {
                this.reader = reader;
                docIdToTimestampMap = getIdToTimestampMap(reader, history);
            }
        }

        @Override
        public void setBottom(final int bottom)
        {
            this.bottom = values[bottom];
        }

        /*
         * Here we populate the map by reading the terms from the reader.
         * We know the issue ids so we can just read the terms and get the sing docId for that term.
         */
        private Map<Integer, Long> getIdToTimestampMap(final IndexReader reader, final List<UserHistoryItem> history)
                throws IOException
        {
            final MapBuilder<Integer, Long> builder = MapBuilder.newBuilder();
            for (UserHistoryItem item : history)
            {
                final Integer docId = getDocIdForIssueId(reader, item.getEntityId());
                // doc Id could be null if issue had been deleted
                if (docId != null)
                {
                    builder.add(docId, item.getLastViewed());
                }
            }
            return builder.toMap();
        }

        /*
        *  Get the document id of the current issue based off key
        */
        private Integer getDocIdForIssueId(final IndexReader reader, final String issueId) throws IOException
        {
            final TermDocs docs = reader.termDocs(new Term(SystemSearchConstants.forLastViewedDate().getIndexField(), issueId));
            return (!docs.next()) ? null : docs.doc();
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

        final UserHistoryFieldComparatorSource that = (UserHistoryFieldComparatorSource) o;

        return (history != null ? history.equals(that.history) : that.history == null);

    }

    public int hashCode()
    {
        return (history != null ? history.hashCode() : 0);
    }
}
