package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Comparator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 * <p/>
 * Used to compare to issue types when they are used as keys in schemes
 */
public class IssueTypeKeyComparator implements Comparator<String>
{
    private final ConstantsManager constantsManager;

    public IssueTypeKeyComparator(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public int compare(String o1, String o2)
    {
        if (o1 == null)
        {
            if (o2 == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if (o2 == null)
            {
                return 1;
            }
            else
            {
                // Both issue types are not null - compare using sequences
                IssueType issueType1 =  constantsManager.getIssueTypeObject(o1);
                IssueType issueType2 = constantsManager.getIssueTypeObject(o2);

                Long sequence1 = issueType1.getSequence();
                Long sequence2 = issueType2.getSequence();

                if (sequence1 == null)
                {
                    if (sequence2 == null)
                    {
                        return 0;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    if (sequence2 == null)
                    {
                        return 1;
                    }
                    else
                    {
                        return sequence1.compareTo(sequence2);
                    }
                }
            }
        }
    }
}
