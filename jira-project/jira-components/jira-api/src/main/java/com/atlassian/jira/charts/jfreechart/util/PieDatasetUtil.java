package com.atlassian.jira.charts.jfreechart.util;

import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities to manipulate datasets
 *
 * @since v4.0
 */
public class PieDatasetUtil
{
    /**
     * Creates a new PieDataset with ranked, consolidated results.
     * <p/>
     * Results are ranked by their value (ie 10 is greater than 5), so that pie slices are in order of greatest to
     * smallest.
     * <p/>
     * The minimum number of items will ALWAYS be displayed if there are at least that many items.
     * <p/>
     * If there are more than the minimum number of items, once ranked any items smaller than the minimumPercent are
     * consolidated into an "Other" entity (named by 'key').
     *
     * @param source the source dataset (<code>null</code> not permitted).
     * @param key the key to represent the aggregated items.
     * @param sort
     * @param minimumPercent the percent threshold (ten percent is 0.10).
     * @param minItems keep at least this many items (no matter how small)
     * @return The pie dataset with (possibly) aggregated items.
     */
    public static PieDataset createConsolidatedSortedPieDataset(PieDataset source, Comparable key, boolean sort,
            double minimumPercent, int minItems)
    {
        final DefaultPieDataset result = new DefaultPieDataset();
        double total = DatasetUtilities.calculatePieDatasetTotal(source);
        if (sort)
        {
            source = createSortedPieDataset(source);
        }

        // Iterate and find all keys below threshold percentThreshold
        // While iterating, build a sorted map of value -> key (ie sorted by value)
        final List keys = source.getKeys();
        final ArrayList otherKeys = new ArrayList();
        Iterator iterator = keys.iterator();
        int minItemsLeft = minItems;
        while (iterator.hasNext())
        {
            Comparable currentKey = (Comparable) iterator.next();
            Number dataValue = source.getValue(currentKey);
            if (dataValue != null)
            {
                double value = dataValue.doubleValue();
                if (minItemsLeft <= 0 && value / total < minimumPercent)
                {
                    otherKeys.add(currentKey);
                }
                else
                {
                    if (minItemsLeft > 0)
                    {
                        minItemsLeft--;
                    }
                }
            }
        }

        //  Create new dataset with keys above threshold percentThreshold
        iterator = keys.iterator();
        double otherValue = 0;
        while (iterator.hasNext())
        {
            Comparable currentKey = (Comparable) iterator.next();
            Number dataValue = source.getValue(currentKey);
            if (dataValue != null)
            {
                if (otherKeys.contains(currentKey))
                {
                    //  Do not add key to dataset
                    otherValue += dataValue.doubleValue();
                }
                else
                {
                    //  Add key to dataset
                    result.setValue(currentKey, dataValue);
                }
            }
        }
        //  Add other category if applicable
        if (otherValue > 0)
        {
            result.setValue(key, new Integer((int) otherValue));
        }
        return result;
    }

    /**
     * Sort a pie dataset by it's values (highest to lowest)
     *
     * @param source the source dataset (<code>null</code> not permitted).
     * @return The pie dataset with sorted items.
     */
    public static PieDataset createSortedPieDataset(PieDataset source)
    {
        List<SortableItem> items = new ArrayList<SortableItem>(source.getItemCount());
        for (final Object o : source.getKeys())
        {
            Comparable key = (Comparable) o;
            Number dataValue = source.getValue(key);
            items.add(new SortableItem(key, dataValue));
        }
        Collections.sort(items);
        Collections.reverse(items);

        DefaultPieDataset sortedDataset = new DefaultPieDataset();
        for (final SortableItem sortableItem : items)
        {
            sortedDataset.setValue(sortableItem.key, sortableItem.value);
        }
        return sortedDataset;
    }

    private static class SortableItem implements Comparable
    {
        private Comparable key;
        private Number value;

        public SortableItem(Comparable key, Number value)
        {
            this.key = key;
            this.value = value;
        }

        public int compareTo(Object o)
        {
            if (o == null)
            {
                return 1;
            }

            Number n = ((SortableItem) o).value;
            if (value.doubleValue() > n.doubleValue())
            {
                return 1;
            }
            else if (value.doubleValue() < n.doubleValue())
            {
                return -1;
            }

            return 0;
        }
    }

}