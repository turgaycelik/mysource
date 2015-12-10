/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWorkflowMigrationResult implements WorkflowMigrationResult
{
    protected final Map<Long, String> failedIssues;

    protected AbstractWorkflowMigrationResult(Map<Long, String> failedIssues)
    {
        if (failedIssues != null)
        {
            this.failedIssues = new HashMap<Long, String>(failedIssues);
        }
        else
        {
            this.failedIssues = Collections.emptyMap();
        }
    }

    public int getNumberOfFailedIssues()
    {
        return this.failedIssues.size();
    }

    public Map<Long, String> getFailedIssues()
    {
        return Collections.unmodifiableMap(this.failedIssues);
    }
}
