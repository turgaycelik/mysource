package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.resolution.Resolution;

import java.util.Comparator;

/**
 * {@link java.util.Comparator} for {@link com.atlassian.jira.issue.resolution.Resolution} based on sequence.
 * See also {@link ResolutionComparator} which is for resolution {@link org.ofbiz.core.entity.GenericValue GenericValues}.
 *
 * @since v4.0
 */
public class ResolutionObjectComparator implements Comparator<Resolution>
{
    /**
     * A {@link java.util.Comparator} that orders {@link com.atlassian.jira.issue.priority.Priority} objects by
     * their configured sequence.
     */
    public static final Comparator<Resolution> RESOLUTION_OBJECT_COMPARATOR = new ResolutionObjectComparator();

    /** Use {@link com.atlassian.jira.issue.comparator.ResolutionObjectComparator#RESOLUTION_OBJECT_COMPARATOR} */
    private ResolutionObjectComparator()
    {
    }

    public int compare(final Resolution resolution, final Resolution resolution1)
    {
        return resolution.getSequence().compareTo(resolution1.getSequence());
    }
}
