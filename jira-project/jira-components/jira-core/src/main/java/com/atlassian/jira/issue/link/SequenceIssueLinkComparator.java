package com.atlassian.jira.issue.link;

import java.util.Comparator;

public class SequenceIssueLinkComparator implements Comparator<IssueLink>
{
    public int compare(final IssueLink issueLink1, final IssueLink issueLink2)
    {
        if (issueLink1 == null || issueLink2 == null)
            throw new IllegalArgumentException("Cannot compare null values");

        Long sequence1 = issueLink1.getSequence();
        Long sequence2 = issueLink2.getSequence();

        if (sequence1 != null && sequence2 != null)
        {
            // If we have both sequences then use their way of comparing
            return sequence1.compareTo(sequence2);
        }
        else if (sequence1 == null && sequence2 == null)
        {
            // If neither sequence exists they objects are equal
            return 0;
        }
        else if (sequence1 == null)
        {
            // o1 should come first as its sequence is null
            return -1;
        }
        else
        {
            // o2 should come first as its sequence is null
            return 1;
        }
    }
}
