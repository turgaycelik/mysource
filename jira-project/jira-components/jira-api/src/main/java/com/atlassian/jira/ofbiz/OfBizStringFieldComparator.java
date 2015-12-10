package com.atlassian.jira.ofbiz;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

/**
 * Comparator that compares two GenericValue objects based on a given String field.
 * <p>
 * Note that the comparison is case insensitive, and orders null at the end.
 *
 * @since v4.3
 */
@PublicApi
public class OfBizStringFieldComparator implements java.util.Comparator<GenericValue>
{
    private final String fieldName;

    public OfBizStringFieldComparator(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public int compare(GenericValue gv1, GenericValue gv2)
    {
        if (gv1 == null && gv2 == null)
        {
            return 0;
        }
        if (gv2 == null)
        {
            // any value is less than null
            return -1;
        }
        if (gv1 == null)
        {
            // null is greater than any value
            return 1;
        }

        String s1 = gv1.getString(fieldName);
        String s2 = gv2.getString(fieldName);

        if (s1 == null && s2 == null)
        {
            return 0;
        }
        else if (s2 == null)
        {
            // any value is less than null
            return -1;
        }
        else if (s1 == null)
        {
            // null is greater than any value
            return 1;
        }
        else
        {
            return s1.compareToIgnoreCase(s2);
        }
    }
}