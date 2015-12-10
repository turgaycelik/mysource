/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.issue.Issue;

/**
 * Class encapsulating behaviour on the "issue picker" popup shown when linking issues.
 */
public interface BugAssociatorPrefs
{
    public static final int RECENT = 1;
    public static final int SEARCH = 7;

    public static final int SINGLE = 17;
    public static final int MULTIPLE = 19;

    /**
     * What to show initially.
     * @return {@link #RECENT} -- show recently viewed issues.
     * {@link #SEARCH} - show a predefined search request's issues (see {@link #getDefaultSearchRequestId}
     */
    public int getDefaultMode(Issue issue);

    /**
     * Whether users can select just one or multiple issues.
     * @param issue
     * @return {@link #SINGLE} - select just one issue or {@link #MULTIPLE} select multiple issues (checkboxes).
     */
    public int getDefaultSearchMode(Issue issue);

    /**
     * Get the search request to run initially, if the {@link #SEARCH} mode is used.
     */
    public long getDefaultSearchRequestId(Issue issue);
}
