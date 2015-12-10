/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.issue.Issue;

public class DefaultBugAssociatorPrefs implements BugAssociatorPrefs
{
    public int getDefaultMode(Issue issue)
    {
        return RECENT;
    }

    public int getDefaultSearchMode(Issue issue)
    {
        return SINGLE;
    }

    public long getDefaultSearchRequestId(Issue issue)
    {
        return -1;
    }
}
