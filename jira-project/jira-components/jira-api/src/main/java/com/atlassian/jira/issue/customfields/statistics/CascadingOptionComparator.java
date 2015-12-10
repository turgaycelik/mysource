package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.option.Option;

import java.util.Comparator;

/**
 * Compares two CascadingSelect options by their sequence numbers.
 *
 * @since v6.1
 */
@Internal
public class CascadingOptionComparator implements Comparator<CascadingOption>
{
    public int compare(CascadingOption o1, CascadingOption o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }
        else if (o1 == null)
        {
            return 1;
        }
        else if (o2 == null)
        {
            return -1;
        }

        // Compare the parents first.
        Option parent1 = o1.getParent();
        Option parent2 = o2.getParent();
        int parentResult = new OptionComparator().compare(parent1, parent2);
        if (parentResult != 0)
            return parentResult;

        // If the parents are identical, compare the children instead.
        Option child1 = o1.getChild();
        Option child2 = o2.getChild();
        return new OptionComparator().compare(child1, child2);
    }
}
