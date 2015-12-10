package com.atlassian.jira.issue.label;

import com.atlassian.annotations.PublicApi;

import java.util.Comparator;

/**
 * Comparator that compares labels by label string
 *
 * @since v4.2
 */
@PublicApi
public class LabelComparator implements Comparator<Label>
{
    public static final LabelComparator INSTANCE = new LabelComparator();

    private LabelComparator()
    {
    }

    public int compare(final Label label1, final Label label2)
    {
        if (label1 == null && label2 == null)
        {
            return 0;
        }

        if (label1 == null)
        {
            return 1;
        }

        if (label2 == null)
        {
            return -1;
        }

        return label1.getLabel().compareTo(label2.getLabel());
    }
}
