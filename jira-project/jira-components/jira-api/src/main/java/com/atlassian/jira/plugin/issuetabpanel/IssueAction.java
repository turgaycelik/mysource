/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.PublicSpi;

import java.util.Date;

/**
 * A comment, work log, changelog etc on an issue.
 * @see IssueTabPanel
 */
@PublicSpi
public interface IssueAction
{
    /**
     * Get the HTML to present for this issueAction on the tab panel. IssueAction represents one entry of the tab panel
     */
    public String getHtml();

    /**
     * This is used to sort between IssueAction objects on the 'All' tab.
     * @return timestamp of when the issue action was created, or throw {@link UnsupportedOperationException} if there
     * is no timestamp (say for generic messages)
     * @see com.atlassian.jira.issue.action.IssueActionComparator
     */
    public Date getTimePerformed();

    /**
     * Determines whether this action is displayed in the 'All' tab
     */
    public boolean isDisplayActionAllTab();
}
