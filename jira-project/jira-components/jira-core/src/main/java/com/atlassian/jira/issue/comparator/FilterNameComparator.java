package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.search.SearchRequest;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare {@link com.atlassian.jira.issue.search.SearchRequest} by its name
 *
 * @since v3.13
 */
public class FilterNameComparator implements Comparator<SearchRequest>, Serializable
{
    public static final Comparator<SearchRequest> COMPARATOR = new FilterNameComparator();

    public int compare(final SearchRequest o1, final SearchRequest o2)
    {
        // check nulls
        if ((o1 == null) && (o2 == null))
        {
            return 0;
        }
        else if (o1 == null) // null is less than any value
        {
            return -1;
        }
        else if (o2 == null) // any value is greater than null
        {
            return 1;
        }

        final String name1 = o1.getName();
        final String name2 = o2.getName();

        // check nulls
        if ((name1 == null) && (name2 == null))
        {
            return 0;
        }
        else if (name1 == null) // null is less than any value
        {
            return -1;
        }
        else if (name2 == null) // any value is greater than null
        {
            return 1;
        }
        return name1.compareToIgnoreCase(name2);
    }
}