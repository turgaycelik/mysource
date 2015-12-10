package com.atlassian.jira.issue.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import com.atlassian.jira.issue.Issue;

/**
* A helper when doing background re-indexing, to match up which issues are seen in the index and database
*
* @since v6.1
*/
public class IndexReconciler implements IssueIdBatcher.Spy
{
    private final long[] indexedIssues;
    private final BitSet matched;
    private final List<Long> unIndexed = new ArrayList<Long>();

    /**
     * Construct a new Reconciler.
     * @param indexedIssues An array of issues known to be in the index.
     */
    public IndexReconciler(final long[] indexedIssues)
    {
        this.indexedIssues = Arrays.copyOf(indexedIssues, indexedIssues.length);
        Arrays.sort(this.indexedIssues);
        matched = new BitSet(indexedIssues.length);
    }

    @Override
    public void spy(final Issue issue)
    {
        // As we see each issue mark it in the bit set as matched.
        int i = Arrays.binarySearch(indexedIssues, issue.getId());
        if (i >= 0)
        {
            matched.set(i);
        }
        else
        {
            unIndexed.add(issue.getId());
        }
    }

    public List<Long> getUnindexed()
    {
        return unIndexed;
    }

    public List<Long> getOrphans()
    {
        List<Long> orphans = new ArrayList<Long>();
        for (int i = matched.nextClearBit(0); i >= 0; i = matched.nextClearBit(i + 1))
        {
            if (i >= indexedIssues.length)
            {
                break;
            }
            orphans.add(indexedIssues[i]);
        }
        return orphans;
    }
}
