package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Comparator;

/**
 * Compares two GenericValue objects based on a given Date field.
 * <p>
 * Note that this Comparator orders null at the end.
 *
 * @since v4.3
 */
public class OfBizDateFieldComparator implements Comparator<GenericValue>
{
    private final String fieldName;

    public OfBizDateFieldComparator(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public int compare(GenericValue gv1, GenericValue gv2)
    {
        if (gv1 == null)
        {
            if (gv2 == null)
            {
                return 0;
            }
            else
            {
                // null is greater than any value
                return 1;
            }
        }
        else if (gv2 == null)
        {
            // any value is less than null
            return -1;
        }

        Timestamp date1 = gv1.getTimestamp(fieldName);
        Timestamp date2 = gv2.getTimestamp(fieldName);

        if (date1 == null)
        {
            if (date2 == null)
            {
                return 0;
            }
            else
            {
                // null is greater than any value
                return 1;
            }
        }
        else if (date2 == null)
        {
            // any value is less than null
            return -1;
        }

        return date1.compareTo(date2);
    }
}