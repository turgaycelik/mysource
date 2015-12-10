/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.Map;

public class WorkflowMigrationSuccess extends AbstractWorkflowMigrationResult
{
    private final SimpleErrorCollection errorCollection;

    /**
     * Construct a result that indicates success.
     * @param failedIssues a map of issue ids to issue keys of issues that
     * failed to migrate during the migration. If there were no failures this
     * must be an empty map.
     */
    public WorkflowMigrationSuccess(Map<Long, String> failedIssues)
    {
        super(failedIssues);
        this.errorCollection = new SimpleErrorCollection();
    }

    public int getResult()
    {
        return SUCCESS;
    }

    public ErrorCollection getErrorCollection()
    {
        return this.errorCollection;
    }

}
