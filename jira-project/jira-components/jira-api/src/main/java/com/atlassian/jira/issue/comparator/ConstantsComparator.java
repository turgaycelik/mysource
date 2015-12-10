package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.IssueConstant;

import java.util.Comparator;

/**
 * Compares GenericValue constants like Priority, Status, Resolution.
 */
public class ConstantsComparator implements Comparator<IssueConstant>
{
    public static final ConstantsComparator COMPARATOR = new ConstantsComparator();

    public int compare(IssueConstant constant1, IssueConstant constant2)
    {
        // If these are the same object then quickly get out of here.
        // When doing issue search sorting Issue Constants are cached and reused so this will often be true.
        if (constant1 == constant2)
            return 0;
        if (constant1 == null)
            return -1;
        if (constant2 == null)
            return 1;

        Long key1 = constant1.getSequence();
        Long key2 = constant2.getSequence();

        if (key1 == null && key2 == null)
            return 0;
        else if (key1 == null)
            return 1;
        else if (key2 == null)
            return -1;

        return key1.compareTo(key2);
    }
}
