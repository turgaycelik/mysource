package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.priority.Priority;

import java.util.Comparator;

/**
 * {@link java.util.Comparator} for {@link com.atlassian.jira.issue.priority.Priority} based on sequence.
 * See also {@link com.atlassian.jira.issue.comparator.PriorityComparator} which is for
 * priority {@link org.ofbiz.core.entity.GenericValue GenericValues}.
 *
 * Priority Sequences are from highest priority to lowest
 *
 * @since v4.0
 */
public class PriorityObjectComparator implements Comparator<Priority>
{
    /**
     * A {@link java.util.Comparator} that orders {@link com.atlassian.jira.issue.priority.Priority} objects by
     * their configured sequence.
     */
    public static final Comparator<Priority> PRIORITY_OBJECT_COMPARATOR = new PriorityObjectComparator();

    /** Use {@link com.atlassian.jira.issue.comparator.PriorityObjectComparator#PRIORITY_OBJECT_COMPARATOR} */
    private PriorityObjectComparator()
    {
    }

    public int compare(final Priority priority, final Priority priority1)
    {
        // The comparison needs to be flipped as the sequence is in the opposite direction
        // of the priorities
        return priority1.getSequence().compareTo(priority.getSequence());
    }
}
