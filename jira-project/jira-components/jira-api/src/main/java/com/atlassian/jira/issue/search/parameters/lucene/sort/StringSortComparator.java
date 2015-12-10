package com.atlassian.jira.issue.search.parameters.lucene.sort;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import java.io.IOException;
import java.util.Arrays;

/**
 * This supplies a Low Memory variant of Lucene's StringOrdValComparator.
 * We have this modified class as we believe that most searches in very large JIRA installations
 * will return a very small portion of the document index, either because they are over only one of
 * many projects or they return only open issues, especially on dashboards and in GreenHopper and similar plugins as
 * well as in general navigator searches.
 */
public class StringSortComparator extends FieldComparatorSource
{
    public StringSortComparator()
    {
    }

    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException
    {
        return new StringOrdValComparator(numHits, fieldname);
    }

    public static final class StringOrdValComparator extends FieldComparator {

        private int[] ords;
        private String[] values;
        private int[] readerGen;

        private int currentReaderGen = -1;
        private String[] lookup;
        private int[] order;
        private final String field;

        private int bottomSlot = -1;
        private int bottomOrd;
        private boolean bottomSameReader;
        private String bottomValue;
        private final int numHits;

        public StringOrdValComparator(int numHits, String field) {
            /* Altassian patch */
            /* Only allocate small arrays initially */
            this.numHits = numHits;
            int initSize = Math.min(1024, numHits);
            ords = new int[initSize];
            values = new String[initSize];
            readerGen = new int[initSize];
            /* Altassian patch end */
            this.field = field;
        }

        @Override
        public int compare(int slot1, int slot2) {
            if (readerGen[slot1] == readerGen[slot2]) {
                return ords[slot1] - ords[slot2];
            }

            final String val1 = values[slot1];
            final String val2 = values[slot2];
            if (val1 == null) {
                if (val2 == null) {
                    return 0;
                }
                return -1;
            } else if (val2 == null) {
                return 1;
            }
            return val1.compareTo(val2);
        }

        @Override
        public int compareBottom(int doc) {
            assert bottomSlot != -1;
            if (bottomSameReader) {
                // ord is precisely comparable, even in the equal case
                return bottomOrd - this.order[doc];
            } else {
                // ord is only approx comparable: if they are not
                // equal, we can use that; if they are equal, we
                // must fallback to compare by value
                final int order = this.order[doc];
                final int cmp = bottomOrd - order;
                if (cmp != 0) {
                    return cmp;
                }

                final String val2 = lookup[order];
                if (bottomValue == null) {
                    if (val2 == null) {
                        return 0;
                    }
                    // bottom wins
                    return -1;
                } else if (val2 == null) {
                    // doc wins
                    return 1;
                }
                return bottomValue.compareTo(val2);
            }
        }

        @Override
        public void copy(int slot, int doc) {
            final int ord = order[doc];

            /* Altassian patch */
            ensureCapacity(slot);
            /* Altassian patch - end */
            ords[slot] = ord;
            assert ord >= 0;
            values[slot] = lookup[ord];
            readerGen[slot] = currentReaderGen;
        }

        /**
         * Atlassian patch to dynamically increase the size of the arrays here as we need them.
         * @param slot Slot to make sure we have capacity to store.
         */
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
                ords = Arrays.copyOf(ords, newSize);
                readerGen = Arrays.copyOf(readerGen, newSize);
            }
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
            FieldCache.StringIndex currentReaderValues = FieldCache.DEFAULT.getStringIndex(reader, field);
            currentReaderGen++;
            order = currentReaderValues.order;
            lookup = currentReaderValues.lookup;
            assert lookup.length > 0;
            if (bottomSlot != -1) {
                setBottom(bottomSlot);
            }
        }

        @Override
        public void setBottom(final int bottom) {
            bottomSlot = bottom;

            bottomValue = values[bottomSlot];
            if (currentReaderGen == readerGen[bottomSlot]) {
                bottomOrd = ords[bottomSlot];
                bottomSameReader = true;
            } else {
                if (bottomValue == null) {
                    ords[bottomSlot] = 0;
                    bottomOrd = 0;
                    bottomSameReader = true;
                    readerGen[bottomSlot] = currentReaderGen;
                } else {
                    final int index = binarySearch(lookup, bottomValue);
                    if (index < 0) {
                        bottomOrd = -index - 2;
                        bottomSameReader = false;
                    } else {
                        bottomOrd = index;
                        // exact value match
                        bottomSameReader = true;
                        readerGen[bottomSlot] = currentReaderGen;
                        ords[bottomSlot] = bottomOrd;
                    }
                }
            }
        }

        @Override
        public Comparable<?> value(int slot) {
            return values[slot];
        }

        public String[] getValues() {
            return values;
        }

        public int getBottomSlot() {
            return bottomSlot;
        }

        public String getField() {
            return field;
        }
    }
    
}
