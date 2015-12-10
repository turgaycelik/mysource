/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.util.Collections;
import java.util.Map;

public class WorkflowMigrationTerminated extends AbstractWorkflowMigrationResult
{
    private final SimpleErrorCollection errorCollection;

    /**
     * Constructs a result that indicates termination due to the fact that errors
     * were found with the data before the migration actaully began and any issues
     * modified.
     * @param errorCollection collection of any errors that were found. Must contain at least
     * one error or error message and must not be null.
     * @throws IllegalArgumentException if null or empty error collection is passed.
     */
    public WorkflowMigrationTerminated(SimpleErrorCollection errorCollection)
    {
        super(Collections.<Long, String> emptyMap());
        if (errorCollection == null || !errorCollection.hasAnyErrors())
        {
            throw new IllegalArgumentException("Must contain errors.");
        }
        else
        {
            this.errorCollection = errorCollection;
        }
    }

    /**
     * Constructs a result that indicates termination of the migration due to the fact that too many
     * issues failed to migrate.
     * @param failedIssues the issue id to issue key map of issues that failed migration
     * until the decision was made to terminate the migration. Must not be null or empty.
     * @throws IllegalArgumentException if null or empty map is passed.
     */
    public WorkflowMigrationTerminated(Map<Long, String> failedIssues)
    {
        super(failedIssues);
        if (failedIssues == null || failedIssues.isEmpty())
        {
            throw new IllegalArgumentException("Must contain failures.");
        }

        this.errorCollection = new SimpleErrorCollection();
    }

    public int getResult()
    {
        return TERMINATED;
    }

    public ErrorCollection getErrorCollection()
    {
        return this.errorCollection;
    }
}
