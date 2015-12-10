/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

public interface IssueUpdater
{
    /**
     * Stores any changes to the issue optionally including a changelog and conditionally dispatches an IssueUpdate
     * event if the changes were real and made to significant fields.
     *
     * @param issueUpdateBean the description of the change.
     * @param generateChangeItems if true, a changelog group is created.
     */
    void doUpdate(IssueUpdateBean issueUpdateBean, boolean generateChangeItems);
}
