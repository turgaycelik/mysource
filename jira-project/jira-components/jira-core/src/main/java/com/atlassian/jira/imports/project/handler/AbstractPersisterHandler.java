package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.imports.project.core.ProjectImportResults;

import java.util.concurrent.Executor;

/**
 * Provides functionality to execute a runnable such that it throws an AbortException if one is found.
 *
 * @since v3.13
 */
public abstract class AbstractPersisterHandler
{
    private final Executor executor;
    private final ProjectImportResults projectImportResults;

    protected AbstractPersisterHandler(final Executor executor, final ProjectImportResults projectImportResults)
    {
        this.executor = executor;
        this.projectImportResults = projectImportResults;
    }

    public void execute(final Runnable runnable) throws AbortImportException
    {
        // First check that we should not abort
        if (projectImportResults.abortImport())
        {
            throw new AbortImportException();
        }

        // If we have passed the error check then lets perform the operation
        executor.execute(runnable);
    }
}
