package com.atlassian.jira.imports.project.handler;

import java.util.concurrent.Executor;

/**
 * Simple executor that runs the runnable in the current thread.
 *
 * @since v3.13
 */
public class ExecutorForTests implements Executor
{
    public void execute(final Runnable runnable)
    {
        runnable.run();
    }
}
